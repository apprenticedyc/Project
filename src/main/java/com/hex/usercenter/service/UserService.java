package com.hex.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hex.usercenter.dto.UserDTO;
import com.hex.usercenter.model.User;

import javax.servlet.http.HttpSession;
import java.util.List;

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
     *
     * @return 是否有管理员权限
     */
    boolean isAdmin(HttpSession httpSession);

    /**
     * 方法重载
     */
    boolean isAdmin(UserDTO user);

    /**
     * 用户脱敏
     *
     * @param user 需要进行脱敏的User
     * @return 脱敏后的用户-用UserDTO封装
     */
    UserDTO getSafetyUser(User user);


    /**
     * 请求用户注销
     *
     * @param httpSession
     * @return
     */
    int userLogout(HttpSession httpSession);

    /**
     * 根据标签查询用户(内存过滤)
     * @param tagNameList 查询用到哪些标签
     * @return 查询出的用户列表
     */
    List<UserDTO> searchUsersByTags(List<String> tagNameList);

    /**
     * 根据标签查询用户(数据库条件语句)
     * @param tagNameList 查询用到哪些标签
     * @return 查询出的用户列表
     */
     List<UserDTO> searchUsersByTagsBySQL(List<String> tagNameList);

    /**
     *
     * @param user 传入的修改后的用户信息
     * @param loginUser 当前登录的用户信息
     * @return
     */
    int updateUser(User user ,UserDTO loginUser);

    /**
     * 获取当前登陆用户信息
     */
    UserDTO getLoginUser(HttpSession session);

}

