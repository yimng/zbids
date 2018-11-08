package com.caicui.app.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.caicui.app.entity.UserLogin;
import com.caicui.app.mapper.UserLoginMapper;
import com.caicui.app.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Service
public class UserLoginServiceImpl implements UserLoginService {
    @Autowired
    private UserLoginMapper userLoginMapper;

    @Override
    public String insertUserLogin(UserLogin userLogin) {
        userLogin.setModifyDate(new Date());
        userLoginMapper.insert(userLogin);
        return userLogin.getUserId();
    }

    @Override
    public UserLogin getUserLoginByUserId(String id) {
        return userLoginMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateUserLogin(UserLogin userLogin) {
        return userLoginMapper.updateByPrimaryKeySelective(userLogin);
    }
}
