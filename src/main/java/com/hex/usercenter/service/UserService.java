package com.hex.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hex.usercenter.dto.UserDTO;
import com.hex.usercenter.model.User;

import javax.servlet.http.HttpSession;

/**
 * 用户服务
 *
 * @author DYC666
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登陆
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    UserDTO userLogin(String userAccount, String userPassword, HttpSession session);

    /**
     * 检验是否有管理权限
     * @return 是否有管理员权限
     */
    boolean isAdmin(HttpSession httpSession);

    /**
     * 用户脱敏
     * @param user 需要进行脱敏的User
     * @return 脱敏后的用户-用UserDTO封装
     */
    UserDTO getSafetyUser(User user);


    /**
     * 请求用户注销
     * @param httpSession
     * @return
     */
    int userLogout(HttpSession httpSession);
}

