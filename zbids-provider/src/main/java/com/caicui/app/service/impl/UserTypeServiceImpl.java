package com.caicui.app.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.caicui.app.entity.UserType;
import com.caicui.app.mapper.UserTypeMapper;
import com.caicui.app.service.UserTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class UserTypeServiceImpl implements UserTypeService {
    @Autowired
    private UserTypeMapper userTypeMapper;

    @Override
    public String insertUserType(UserType userType) {
        userType.setCreateDate(new Date());
        userTypeMapper.insert(userType);
        return userType.getUserUsertypeId();
    }

    @Override
    public UserType getUserTypeByUserId(String userId) {
        Example example = new Example(UserType.class);
        example.createCriteria().andEqualTo("userId", userId);
        return userTypeMapper.selectOneByExample(example);
    }

    @Override
    public int updateUserType(UserType userType) {
        return userTypeMapper.updateByPrimaryKey(userType);
    }
}
