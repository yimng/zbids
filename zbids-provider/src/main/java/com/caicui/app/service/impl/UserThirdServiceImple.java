package com.caicui.app.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.caicui.app.entity.UserThird;
import com.caicui.app.mapper.UserThirdMapper;
import com.caicui.app.service.UserThirdService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;


@Service
public class UserThirdServiceImple implements UserThirdService {
    @Autowired
    private UserThirdMapper userThirdMapper;

    @Override
    public UserThird getUserThirdBySocietyId(int thirdType, String thirdUserid) {
        Example example = new Example(UserThird.class);
        example.createCriteria().andEqualTo("thirdType", thirdType).andEqualTo("thirdUserid", thirdUserid);
        return userThirdMapper.selectOneByExample(example);
    }

    @Override
    public String insertUserThird(UserThird userThird) {
        userThird.setCreateDate(new Date());
        userThirdMapper.insert(userThird);
        return userThird.getThirdUserid();
    }

    @Override
    public UserThird getUserThirdByUserId(String userId) {
        return userThirdMapper.selectByPrimaryKey(userId);
    }

    @Override
    public int deleteUserThird(String userid) {
        Example example = new Example(UserThird.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", userid);
        return userThirdMapper.deleteByExample(example);
    }
}
