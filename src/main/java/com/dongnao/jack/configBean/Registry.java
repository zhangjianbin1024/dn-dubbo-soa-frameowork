package com.dongnao.jack.configBean;

import com.dongnao.jack.registry.BaseRegistry;
import com.dongnao.jack.registry.RedisRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册中心
 */
@Slf4j
public class Registry extends BaseConfigBean implements ApplicationContextAware {

    public ApplicationContext application;

    //多种方式实现注册中心的具体实现
    private static Map<String, BaseRegistry> registryMap = new HashMap();

    /**
     * @Fields serialVersionUID TODO 
     */
    static {
        registryMap.put("redis", new RedisRegistry());
    }

    private static final long serialVersionUID = 45672141098765L;

    private String protocol;

    private String address;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.application = applicationContext;

    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static Map<String, BaseRegistry> getRegistryMap() {
        return registryMap;
    }

    public static void setRegistryMap(Map<String, BaseRegistry> registryMap) {
        Registry.registryMap = registryMap;
    }
}
