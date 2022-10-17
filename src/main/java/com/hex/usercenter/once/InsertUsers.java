package com.hex.usercenter.once;

import com.hex.usercenter.mapper.UserMapper;
import com.hex.usercenter.model.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * @author Hex
 * @since 2022/10/4
 * Description
 */
@Component
public class InsertUsers {
    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入用户
     */
    //@Scheduled(fixedRate = Long.MAX_VALUE)
    public void doInsert() {
        StopWatch stopWatch = new StopWatch(); //计时工具
        stopWatch.start();
        final int INSERT_NUM = 1;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserName("测试用户");
            user.setUserAccount("FakeDingZhen");
            user.setUserPassword("12345678");
            user.setGender(0);
            user.setAvatarUrl("https://xingqiu-tuchuang-1256524210.cos.ap-shanghai.myqcloud.com/4188/QQ截图20220503132910.png");
            user.setEmail("114514@qq.com");
            user.setUserStatus(0);
            user.setPhone("139114514");
            user.setAuthority(0);
            user.setTags("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println("执行插入所用时间:" + stopWatch.getTotalTimeMillis());
    }
}
