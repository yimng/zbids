package com.caicui;

import com.caicui.app.entity.UserThird;
import com.caicui.app.service.UserThirdService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ZbidsProviderApplicationTests {


    @Autowired
    private UserThirdService service;

    @Test
    public void test() {
        UserThird userThird = service.getUserThirdBySocietyId(1, "dsfdsf");

    }
}
