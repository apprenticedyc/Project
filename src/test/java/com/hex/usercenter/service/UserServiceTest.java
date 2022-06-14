package com.hex.usercenter.service;

import com.hex.usercenter.model.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Hex
 * @version 1.0
 * @since 2022/5/15
 * Description
 */
@SpringBootTest
class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    void testAddUser() {
        User user = new User();
        user.setUserName("Andy");
        user.setUserAccount("123");
        user.setUserPassword("123");
        user.setGender(0);
        user.setAvatarUrl("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fi0.hdslb.com%2Fbfs%2Farticle%2Fe7862cbbb3785881a4290321d7510400cd4367c8.jpg&refer=http%3A%2F%2Fi0.hdslb.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1655200953&t=001ae67a1c529f3aca85e651363d5353");
        user.setEmail("123");
        user.setUserStatus(0);
        user.setIsDelete(0);
        boolean isSaved = userService.save(user);
        assertTrue(isSaved);
    }

    @Test
    void userRegister() {
        String userAccount = "hex123";
        String userPassword = "123456789";
        long l = userService.userRegister(userAccount, userPassword, "123456789");
        Assert.assertTrue(l>0);
    }
}