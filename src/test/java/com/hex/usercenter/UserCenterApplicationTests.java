package com.hex.usercenter;

import com.hex.usercenter.mapper.UserMapper;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class UserCenterApplicationTests {
    @Resource
    private UserMapper userMapper;
}
