package com.hex.usercenter.constant;

/**
 * @author Hex
 * @version 1.0
 * @since 2022/5/16
 * Description
 */
public interface UserConstant {
    /**
     * 盐值混淆密码
     */
    public static final String SALT = "hex";
    /**
     * 保存用户状态的key
     */
    public static final String USER_LOGIN_STATE = "userLoginState";
    /**
     * 普通用户权限等级
     */
    public static final int AUTHORITY_DEFAULT = 0;
    /**
     * 管理员权限等级
     */
    public static final int AUTHORITY_ADMIN = 1;
    /**
     * 超管权限等级
     */
    public static final int AUTHORITY_SUPERVISOR = 2;
}
