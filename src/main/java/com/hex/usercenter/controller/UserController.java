package com.hex.usercenter.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hex.usercenter.common.ErrorCode;
import com.hex.usercenter.common.Result;
import com.hex.usercenter.dto.UserDTO;
import com.hex.usercenter.dto.UserLoginRequestDTO;
import com.hex.usercenter.dto.UserRegisterRequestDTO;
import com.hex.usercenter.model.User;
import com.hex.usercenter.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

import static com.hex.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author Hex
 * @version 1.0
 * @since 2022/5/17
 * Description
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 注册
     *
     * @param user 前端传来的用户信息
     * @return
     */
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
            return Result.fail(ErrorCode.PARAMS_NULL_ERROR,"Session域断开");
        }
        int result = userService.userLogout(httpSession);
        return Result.ok(result);
    }

    /**
     * 获取当前登录用户的信息
     */
    @GetMapping("/current")
    public Result<UserDTO> getCurrentUser(HttpSession httpSession) {
        UserDTO currentUser = (UserDTO) httpSession.getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) {
            return Result.fail(ErrorCode.NOT_LOGIN,"用户未登录");
        }
        //todo 将从数据库查询用户数据改为从redis中查询
        User byId = userService.getById(currentUser.getId());
        UserDTO safetyUser = userService.getSafetyUser(byId);
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
        if (userService.isAdmin(httpSession)) {
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

    /**
     * 逻辑删除用户 (仅管理员可用)
     *
     * @param id 被删除的用户id
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteUser(Long id, HttpSession httpSession) {
        //先判断是否是管理员
        if (userService.isAdmin(httpSession)) {
            return Result.fail(ErrorCode.NO_AUTHORITY_ERROR,"无管理员权限");
        }
        if (id <= 0) {
            return Result.fail(ErrorCode.PARAMS_ERROR,"要删除的用户id不能小于0");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        //模糊查询
        userQueryWrapper.like("id", id);
        //在配置文件中开启了逻辑删除功能 这个方法会进行逻辑删除 也就是更新数据而不是真的删除
        boolean result = userService.removeById(id);
        return Result.ok(result);
    }


}
