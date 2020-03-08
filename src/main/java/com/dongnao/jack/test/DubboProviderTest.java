package com.dongnao.jack.test;

import com.dongnao.jack.test.service.TestService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: zh
 * @date: 2020/3/1/001 19:03
 */
public class DubboProviderTest {
    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext(
                "mytest.xml");

        TestService testService = app.getBean(TestService.class);
        testService.eat("xxxxx");
    }
}
