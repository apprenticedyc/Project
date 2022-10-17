package com.hex.usercenter.scheduleJob;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hex.usercenter.model.User;
import com.hex.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hex.usercenter.constant.RedisConstant.redisKeyFormat;

/**
 * @author Hex
 * @since 2022/10/15
 * Description 缓存预热任务
 */
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 每天执行 加载预热用户
     */
    @Scheduled(cron = "0 02 23 * * *") //cron表达式指定该任务在每天的23:59执行
    public void doCacheRecommendUser() {
        List<Long> mainUser = Arrays.asList(1L,5L); //重点用户的缓存内容需要预热比如老板,组长
        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        for (Long userId : mainUser) {
            for (int current = 1; current <= 10; current++) { //为每个用户缓存前10页的推荐内容
                String redisKey = String.format(redisKeyFormat, userId,current); //加上页数
                // 没有缓存 去数据库查询 并且写入redis
                QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                //改成分页查询
               Page<User> userPage = userService.page(new Page<>(current,8 ), userQueryWrapper);
                // 写入缓存 失败了捕获异常
                try {
                    opsForValue.set(redisKey, userPage, 300000, TimeUnit.MILLISECONDS); //设置过期时间300000毫秒300秒
                } catch (Exception e) {
                    log.error("redis set key error", e);
                }
            }
        }
    }
}
