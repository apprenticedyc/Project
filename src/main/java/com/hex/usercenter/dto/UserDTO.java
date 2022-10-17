package com.hex.usercenter.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 脱敏后的用户类
 */
@Data
public class UserDTO implements Serializable {
    private String userName;
    private long id;
    private String userAccount;
    private Integer gender;
    private String avatarUrl;
    private String email;
    private String userStatus;
    private String phone;
    private Integer authority;
    private Date createTime;
    private String tags;
}
