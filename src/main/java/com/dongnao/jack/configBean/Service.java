package com.dongnao.jack.configBean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.dongnao.jack.registry.BaseRegistryDelegate;

/**
 * 生产者
 */
public class Service extends BaseConfigBean implements InitializingBean,
        ApplicationContextAware {

    /**
     * @Fields serialVersionUID TODO
     */

    private static final long serialVersionUID = 2300087543452L;

    //服务的接口
    private String intf;

    // 服务的实现者
    private String ref;

    private String protocol;

    private static ApplicationContext application;

    public static ApplicationContext getApplication() {
        return application;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        Service.application = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //服务注册信息委托给其它类完成
        BaseRegistryDelegate.registry(ref, application);

        // redis 发布内容，即新的节点信息
        // RedisApi.publish("channel" + ref, "这个内容要跟redis里面的节点内容一致");
    }

    public String getIntf() {
        return intf;
    }

    public void setIntf(String intf) {
        this.intf = intf;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
