package com.dongnao.jack.registry;

import com.dongnao.jack.configBean.Registry;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class BaseRegistryDelegate {

    /**
     * 向注册中心注册信息
     *
     * @param ref         配置的服务实现id
     * @param application
     */
    public static void registry(String ref, ApplicationContext application) {
        // 注册中心对象
        Registry registry = application.getBean(Registry.class);
        String protocol = registry.getProtocol();

        //获取配置的注册协议
        BaseRegistry registryBean = Registry.getRegistryMap().get(protocol);
        registryBean.registry(ref, application);
    }

    /**
     * 获取注册信息
     *
     * @param id          reference 标签所配置的id
     * @param application
     * @return
     */
    public static List<String> getRegistry(String id,
                                           ApplicationContext application) {
        Registry registry = application.getBean(Registry.class);
        String protocol = registry.getProtocol();
        BaseRegistry registryBean = Registry.getRegistryMap().get(protocol);

        return registryBean.getRegistry(id, application);
    }
}
