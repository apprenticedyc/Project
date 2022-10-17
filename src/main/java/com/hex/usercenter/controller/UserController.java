package com.hex.usercenter.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hex.usercenter.common.ErrorCode;
import com.hex.usercenter.common.Result;
import com.hex.usercenter.dto.UserDTO;
import com.hex.usercenter.dto.UserLoginRequestDTO;
import com.hex.usercenter.dto.UserRegisterRequestDTO;
import com.hex.usercenter.exception.BusinessException;
import com.hex.usercenter.model.User;
import com.hex.usercenter.service.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hex.usercenter.constant.RedisConstant.redisKeyFormat;

/**
 * @author Hex
 * @version 1.0
 * @since 2022/5/17
 * Description
 */
@Api(tags = "用户模块") //knife4j注解标识模块
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8000"}, allowCredentials = "true")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate; //引入自定义的redisTemplate

    /**
     * 注册
     *
     * @param user 前端传来的用户信息
     * @return
     */
    //value 简单描述，notes 详细描述
    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterRequestDTO user) { //使用@RequestBody UserRequestDTO user对前端传来的请求体中的json数据进行封装
        if (user == null) {
            return Result.fail(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = user.getUserAccount();
        String userPassword = user.getUserPassword();
        String checkPassword = user.getCheckPassword();
        if (CharSequenceUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            return Result.fail(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return Result.ok(result);
    }

    /**
     * 登陆
     *
     * @param user        前端传来的用户信息
     * @param httpSession session域维持登录态
     */
    @PostMapping("/login")
    public Result<UserDTO> userLogin(@RequestBody UserLoginRequestDTO user, HttpSession httpSession) {
        if (user == null) {
            return Result.fail(ErrorCode.PARAMS_NULL_ERROR, "请求参数为空");
        }
        String userAccount = user.getUserAccount();
        String userPassword = user.getUserPassword();
        if (CharSequenceUtil.hasBlank(userAccount, userPassword)) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "账号或密码为空");
        }
        UserDTO userDTO = userService.userLogin(userAccount, userPassword, httpSession);
        return Result.ok(userDTO);
    }

    /**
     * 注销登录
     */
    @PostMapping("/logout")
    public Result<Integer> userLogout(HttpSession httpSession) {
        if (httpSession == null) {
            return Result.fail(ErrorCode.PARAMS_NULL_ERROR, "Session域断开");
        }
        int result = userService.userLogout(httpSession);
        return Result.ok(result);
    }

    /**
     * 获取当前登录用户的信息
     */
    @GetMapping("/current")
    public Result<UserDTO> getCurrentUser(HttpSession httpSession) {
        UserDTO currentUser = userService.getLoginUser(httpSession);
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        UserDTO safetyUser = userService.getSafetyUser(user);
        return Result.ok(safetyUser);
    }

    /**
     * 查询所有用户信息 仅管理员可见
     *
     * @param username 被查询的用户名
     */
    @GetMapping("/query")
    public Result<List<UserDTO>> queryUsers(String username, HttpSession httpSession) {
        //先判断是不是管理员
        if (!userService.isAdmin(httpSession)) {
            return Result.fail(ErrorCode.NO_AUTHORITY_ERROR);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(username)) {
            //模糊查询
            userQueryWrapper.like("user_name", username);
        }
        List<User> userList = userService.list(userQueryWrapper);
        List<UserDTO> userDTOList = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return Result.ok(userDTOList);
    }

    @GetMapping("/query/tags")
    public Result<List<UserDTO>> queryByTags(@RequestParam(required = false) List<String> tagNameList) {
        // 多一份检查 多一份保障
        if (CollectionUtil.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜寻标签列表不能为空");
        }
        return Result.ok(userService.searchUsersByTags(tagNameList));
    }

    @GetMapping("/recommend")
    public Result<Page<UserDTO>> recommendUsers(long pageSize, long current, HttpSession httpSession) {
        /************改进**************/
        //如果有缓存就读缓存 ,如果没有 就去数据库里读并将数据写入缓存
        long userID = userService.getLoginUser(httpSession).getId(); //获取当前登录用户的id作为redisKey的一部分
        String redisKey = String.format(redisKeyFormat, userID,current); //加上页数
        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) opsForValue.get(redisKey);
        if (userPage != null) {
            return Result.ok(userPage);
        }
        // 没有缓存 去数据库查询 并且写入redis
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        //改成分页查询
        userPage = userService.page(new Page<>(current, pageSize), userQueryWrapper);
        // 写入缓存 失败了捕获异常
        try {
            opsForValue.set(redisKey, userPage,30000, TimeUnit.MILLISECONDS); //设置过期时间30000毫秒30秒
        } catch (Exception e) {
          log.error("redis set key error",e);
        }
        return Result.ok(userPage);
    }

    /**
     * 逻辑删除用户 (仅管理员可用)
     *
     * @param id 被删除的用户id
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteUser(Long id, HttpSession httpSession) {
        //先判断是否是管理员
        if (!userService.isAdmin(httpSession)) {
            return Result.fail(ErrorCode.NO_AUTHORITY_ERROR, "无管理员权限");
        }
        if (id <= 0) {
            return Result.fail(ErrorCode.PARAMS_ERROR, "要删除的用户id不能小于0");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        //模糊查询
        userQueryWrapper.like("id", id);
        //在配置文件中开启了逻辑删除功能 这个方法会进行逻辑删除 也就是更新数据而不是真的删除
        boolean result = userService.removeById(id);
        return Result.ok(result);
    }

    @PostMapping("/update")
    public Result<Integer> updateUser(@RequestBody User user, HttpSession session) {
        // 1. 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        UserDTO loginUser = userService.getLoginUser(session);
        return Result.ok(userService.updateUser(user, loginUser));
    }
}
