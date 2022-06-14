package com.hex.usercenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hex.usercenter.common.ErrorCode;
import com.hex.usercenter.dto.UserDTO;
import com.hex.usercenter.exception.BusinessException;
import com.hex.usercenter.mapper.UserMapper;
import com.hex.usercenter.model.User;
import com.hex.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

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

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
          throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名太短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码太短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }
        //账号不包含特殊字符
        if (!ReUtil.isMatch(Validator.GENERAL, userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符");
        }
        //账户不能重复
        //设置查询条件
        QueryWrapper<User> countQueryWrapper = new QueryWrapper<>();
        countQueryWrapper.eq("user_account", userAccount);
        //查询记录数大于0表示已经有人注册了
        long count = this.count(countQueryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户保存失败");
        }
        return user.getId();
    }

    @Override
    public UserDTO userLogin(String userAccount, String userPassword, HttpSession session) {
        // 1.校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名太短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码太短");
        }
        //账号不包含特殊字符
        if (!ReUtil.isMatch(Validator.GENERAL, userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符");
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码错误");
        }
        //Todo 可以添加用户登陆限流 防止一段时间内重复登陆多次

        //4.记录用户登录态
        //生成随机token 作为登陆令牌
        String token = UUID.randomUUID().toString(true);
        //todo 此处先保存脱敏后的用户信息到session中以后保存到redis中
        UserDTO safetyUser = getSafetyUser(user);
        session.setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    @Override
    public boolean isAdmin(HttpSession httpSession) {
        //先获取Session中的用户信息 todo 以后换成从redis中取数据
        UserDTO user = (UserDTO) httpSession.getAttribute(USER_LOGIN_STATE);
        if (user == null || user.getAuthority() == AUTHORITY_DEFAULT) {
            return true;
        }
        return false;
    }

    @Override
    public UserDTO getSafetyUser(User user) {
        if (user==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"需要脱敏的用户数据为空");
        }
        return BeanUtil.copyProperties(user, UserDTO.class);
    }

    @Override
    public int userLogout(HttpSession httpSession) {
        httpSession.removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

}




