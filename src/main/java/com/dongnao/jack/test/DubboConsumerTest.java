package com.dongnao.jack.test;

import com.dongnao.jack.test.service.TestService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: zh
 * @date: 2020/3/1/001 19:02
 */
public class DubboConsumerTest {
    /**
     * 测试消费端 reference
     *
     * @param args
     */
    public static void main(String[] args) {
        ApplicationContext app = new ClassPathXmlApplicationContext(
                "mytest.xml");
        // 获取代理对象，会调用 Reference#getObject方法中
        TestService tests = app.getBean(TestService.class);
        // 会调用到 InvokeInvocationHandler#invoke方法中
        tests.eat("xxxgrer");
    }
}
