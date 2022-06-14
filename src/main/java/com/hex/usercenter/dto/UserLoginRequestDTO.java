package com.hex.usercenter.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Hex
 * @version 1.0
 * @since 2022/5/17
 * Description
 */
@Data
public class UserLoginRequestDTO implements Serializable {
    private String userAccount;
    private String userPassword;
}
