package com.caicui.app.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.caicui.app.dmo.App;
import com.caicui.app.dmo.AppChecktokenLog;
import com.caicui.app.dmo.AppGettokenLog;
import com.caicui.app.mapper.AppMapper;
import com.caicui.app.service.EduAppService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

@Service
public class EduAppServiceImpl implements EduAppService {
    @Autowired
    private AppMapper appMapper;

    @Override
    public List<App> selectApp(App a) {
        // TODO Auto-generated method stub
        return appMapper.selectApp(a);
    }

    @Override
    public App selectAppById(String id) {
        // TODO Auto-generated method stub
        return appMapper.selectAppById(id);
    }

    @Override
    public int insertAppChecktokenLog(AppChecktokenLog log) {
        // TODO Auto-generated method stub
        log.setCreateDate(new Date());
        return appMapper.insertAppChecktokenLog(log);
    }

    @Override
    public int insertAppGettokenLog(AppGettokenLog log) {
        // TODO Auto-generated method stub
        log.setCreateDate(new Date());
        return appMapper.insertAppGettokenLog(log);
    }

    @Override
    public App selectAppByAppId(String appId) {

        return appMapper.selectAppByAppId(appId);
    }

}
