package com.hex.usercenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hex.usercenter.common.ErrorCode;
import com.hex.usercenter.dto.UserDTO;
import com.hex.usercenter.exception.BusinessException;
import com.hex.usercenter.mapper.UserMapper;
import com.hex.usercenter.model.User;
import com.hex.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hex.usercenter.constant.UserConstant.*;


/**
 * 用户服务实现类
 *
 * @author DYC666
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate<String ,Object> redisTemplate;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名太短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码太短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        //账号不包含特殊字符
        if (!ReUtil.isMatch(Validator.GENERAL, userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        //账户不能重复
        //设置查询条件
        QueryWrapper<User> countQueryWrapper = new QueryWrapper<>();
        countQueryWrapper.eq("user_account", userAccount);
        //查询记录数大于0表示已经有人注册了
        long count = this.count(countQueryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }

        //2. 对密码进行加密(加盐并使用MD5算法加密)
        byte[] bytes = DigestUtil.md5(SALT + userPassword);
        String encryptPassword = new String(bytes);
        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户保存失败");
        }
        return user.getId();
    }

    @Override
    public UserDTO userLogin(String userAccount, String userPassword, HttpSession session) {
        // 1.校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名太短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码太短");
        }
        //账号不包含特殊字符
        if (!ReUtil.isMatch(Validator.GENERAL, userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }

        //2. 对密码进行加密(加盐并使用MD5)
        //给输入的密码加盐值用于和数据库中的密码进行比较
        byte[] bytes = DigestUtil.md5(SALT + userPassword);
        String encryptPassword = new String(bytes);

        //3.查询用户是否存在
        //设置查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        //查询
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        //Todo 可以添加用户登陆限流 防止一段时间内重复登陆多次

        //4.记录用户登录态
        //todo 生成随机token 作为登陆令牌
        String token = UUID.randomUUID().toString(true);
        //todo 此处先保存脱敏后的用户信息到session中以后保存到redis中
        UserDTO safetyUser = getSafetyUser(user);
        session.setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    @Override
    public boolean isAdmin(HttpSession httpSession) {
        //从redis中取数据
        UserDTO user = (UserDTO) httpSession.getAttribute(USER_LOGIN_STATE);
        if (user == null || user.getAuthority() == AUTHORITY_DEFAULT) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isAdmin(UserDTO user) {
        //从redis中取数据
        if (user == null || user.getAuthority() == AUTHORITY_DEFAULT) {
            return false;
        }
        return true;
    }

    @Override
    public UserDTO getSafetyUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "需要脱敏的用户数据为空");
        }
        return BeanUtil.copyProperties(user, UserDTO.class);
    }

    @Override
    public int userLogout(HttpSession httpSession) {
        httpSession.removeAttribute(USER_LOGIN_STATE);
        return 1;
    }


    @Override
    public List<UserDTO> searchUsersByTags(List<String> tagNameList) {
        // 获取所有数据 然后在内存中进行筛选
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        List<User> users = userMapper.selectList(userQueryWrapper);
        //先查询所有数据
        //在内存中计算是否含有要求的标签 优点: 更加灵活
        Gson gson = new Gson(); // GSON: 序列化工具
        List<UserDTO> userDTOList = users.stream().filter(user -> { // 使用StreamAPI批量处理数据
            String tagStr = user.getTags();// 获取每个用户的标签列表(Json字符串)
            if (StrUtil.isBlank(tagStr)) { //过滤标签为空的用户
                return false;
            }
            Set<String> userTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>() {
            }.getType());// 使用Gson将Json字符串反序列化为Set<String>对象

            Optional.ofNullable(userTagNameSet).orElse(new HashSet<>()); // 用Optional类包裹避免出现空指针异常 如果为空就提供一个new HashSet
            for (String tagName : tagNameList) {
                if (!userTagNameSet.contains(tagName)) { // 如果用户的标签列表中没有查询条件中的标签 就返回false
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
        return userDTOList;
    }

    @Override
    public List<UserDTO> searchUsersByTagsBySQL(List<String> tagNameList) {
        //   1. 在querywrapper中设置查询条件
        if (CollectionUtil.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签不能为空!查询用户失败");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            userQueryWrapper = userQueryWrapper.like("tags", tagName);
        }
        List<User> users = userMapper.selectList(userQueryWrapper);
        return users.stream().map(this::getSafetyUser).collect(Collectors.toList()); // Java8新特性 StreamAPI
    }


    // todo 待完成
    @Override
    public int updateUser(User user, UserDTO loginUser) {
        // 当前登录的用户的id
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //查询要更新的用户是否存在
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        // 1. 校验权限 仅限管理员和自己本人能修改
        // 如果是管理员允许更新任意用户
        // 如果不是管理员只能更新自己的信息也就是Id相等的用户 如果不是本人那么拒绝修改返回无权限
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTHORITY_ERROR);
        }
        // 2. 触发更新
        return userMapper.updateById(user); //返回更新条数 更新成功就是一条返回1
    }

    @Override
    public UserDTO getLoginUser(HttpSession session) {
        UserDTO currentUser = (UserDTO) session.getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return currentUser;
    }
}




