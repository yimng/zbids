package com.caicui.commons.token.impl;

import com.caicui.commons.assertion.CommonAssert;
import com.caicui.commons.token.TokenService;
import com.caicui.redis.cache.redis.service.RedisRawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * com.caicui.course.web.command.service.impl
 * Created by yukewi on 2015/5/25 10:11.
 */

@Component
@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private RedisRawService redisRawService;

    /**
     * 根据token获取MemberId
     *
     * @param token
     * @return
     */
    @Override
    public String getMemberId(String token) {
        CommonAssert.assertNotNull("token不能为空", token);
        final byte[] o = redisRawService.get(token);
        if (o == null) {
            throw new RuntimeException("用户未登录");
        }
        String memberId = new String(o);
        redisRawService.set(token, memberId, 60 * 60L);
        return memberId;
    }

}
