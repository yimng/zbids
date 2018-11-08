package com.caicui.app.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.caicui.app.entity.User;
import com.caicui.app.mapper.UserMapper;
import com.caicui.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;


    @Override
    public User getUserByPhone(String s) {

        User user = new User();
        user.setMobile(s);
        return userMapper.selectOne(user);

    }

    @Override
    public User getUserByEmail(String email) {
        User user = new User();
        user.setEmail(email);
        return userMapper.selectOne(user);
    }

    @Override
    public User getUserByNickname(String s) {
        User user = new User();
        user.setLoginName(s);
        return userMapper.selectOne(user);
    }

    @Override
    public User getUserById(String s) {
        return userMapper.selectByPrimaryKey(s);
    }

    @Override
    public int updateUser(User user) {
        return userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public String insertUser(User appMember) {
        appMember.setModifyDate(new Date());
        userMapper.insert(appMember);
        return appMember.getUserId();
    }


}
