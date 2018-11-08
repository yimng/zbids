package com.caicui.app.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.caicui.app.dmo.AppLoginLog;
import com.caicui.app.mapper.AppLoginLogMapper;
import com.caicui.app.service.EduAppLoginLogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class EduAppLoginLogServiceImpl implements EduAppLoginLogService {
    @Autowired
    private AppLoginLogMapper appLoginLogMapper;

    @Override
    public int insertAppLoginLog(AppLoginLog log) {
        // TODO Auto-generated method stub
        return appLoginLogMapper.insertAppChecktokenLog(log);
    }

    @Override
    public Map<String, Object> selectAppLoginLogByPage(String memberId,
                                                       Integer pageNo, Integer pageSize) {
        int pageIndex = -1;
        if (pageNo > 0 && pageSize > 0) {
            pageIndex = (pageNo - 1) * pageSize;
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<AppLoginLog> msgList = appLoginLogMapper.selectAppLoginLog(memberId, pageIndex, pageSize);
        resultMap.put("data", msgList);//数据
        int totalCount = appLoginLogMapper.selectAppLoginLogCount(memberId);
        resultMap.put("totalCount", totalCount);
        resultMap.put("pageNo", pageNo);
        resultMap.put("pageSize", pageSize);
        return resultMap;
    }

}
