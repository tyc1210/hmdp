package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.exception.BaseException;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.ICacheService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.JwtHelper;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private ICacheService cacheService;

    @Override
    public String login(LoginFormDTO loginForm) {
        // 校验手机号是否正确
        if(RegexUtils.isPhoneInvalid(loginForm.getPhone())){
            throw new BaseException("手机号错误");
        }
        // 检验code
        String code = cacheService.getCode(loginForm.getPhone());
        if (StringUtils.isEmpty(code) || !loginForm.getCode().equals(code)) {
            throw new BaseException("验证码错误");
        }
        // 查询数据库是否存在
        User user = query().eq("phone", loginForm.getPhone()).one();
        if(null == user){
            // 不存在则创建
            user = createUserWithPhone(loginForm.getPhone());
        }
        // 缓存用户信息
//        cacheService.saveUser(user);
        // 创建token
        return JwtHelper.createToken(user.getId(),user.getNickName());
    }

    @Override
    public void sendCode(String phone) {
        // 校验手机号是否正确
        if(RegexUtils.isPhoneInvalid(phone)){
            throw new BaseException("手机号格式错误");
        }
        // 生成短信验证码
        String code = RandomUtil.randomNumbers(6);
        // 记录验证码
        cacheService.saveCode(phone,code);
        // todo 调用第三方服务发送短信
        log.info("向手机号：{},发送验证码：{}",phone,code);
    }

    private User createUserWithPhone(String phone) {
        // 1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 2.保存用户
        save(user);
        return user;
    }
}
