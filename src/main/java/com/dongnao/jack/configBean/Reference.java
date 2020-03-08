package com.dongnao.jack.configBean;

import com.dongnao.jack.cluster.Cluster;
import com.dongnao.jack.cluster.FailfastClusterInvoke;
import com.dongnao.jack.cluster.FailoverClusterInvoke;
import com.dongnao.jack.cluster.FailsafeClusterInvoke;
import com.dongnao.jack.invoke.HttpInvoke;
import com.dongnao.jack.invoke.Invoke;
import com.dongnao.jack.invoke.NettyInvoke;
import com.dongnao.jack.invoke.RmiInvoke;
import com.dongnao.jack.loadbalance.LoadBalance;
import com.dongnao.jack.loadbalance.RandomLoadBalance;
import com.dongnao.jack.loadbalance.RoundRobinLoadBalance;
import com.dongnao.jack.proxy.advice.InvokeInvocationHandler;
import com.dongnao.jack.registry.BaseRegistryDelegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费都端的配置，并引用生产者服务者的配置
 */
@Slf4j
public class Reference extends BaseConfigBean implements FactoryBean,
        InitializingBean, ApplicationContextAware {

    private static final long serialVersionUID = 334571190L;

    //接口的字符串
    private String intf;

    private String loadbalance;

    private String protocol;

    private static ApplicationContext application;

    private Invoke invoke;

    private String cluster;

    private String retries;


    /**
     * rpc 调用协议实现列表
     */
    private static Map<String, Invoke> invokes = new HashMap<String, Invoke>();

    /**
     * 负载均衡实现列表
     */
    private static Map<String, LoadBalance> loadBalances = new HashMap<String, LoadBalance>();

    /**
     * 集群容错实现列表
     */
    private static Map<String, Cluster> clusters = new HashMap<String, Cluster>();

    /**
     * registryInfo 这个是生产者的多个服务的列表
     */

    private List<String> registryInfo = new ArrayList<String>();

    static {
        //调用时的具体协议实现对象注册
        invokes.put("http", new HttpInvoke());
        invokes.put("rmi", new RmiInvoke());
        invokes.put("netty", new NettyInvoke());
        log.info("初始化注册协议实现");

        loadBalances.put("romdom", new RandomLoadBalance());
        loadBalances.put("roundrob", new RoundRobinLoadBalance());
        log.info("初始化负载均衡实现");

        clusters.put("failover", new FailoverClusterInvoke());
        clusters.put("failfast", new FailfastClusterInvoke());
        clusters.put("failsafe", new FailsafeClusterInvoke());
        log.info("初始化集群容错实现");
    }


    /**
     * 这个方法是 spring 调用的
     * ApplicationContext.getBean("beanId")时，就会调用这个方法，
     * 这个方法的返回值，会交给spring 容器进行管理
     * <p>
     * <p>
     * 这里其实返回的是 interface 配置的接口代理类
     *
     * @return
     * @throws Exception
     */
    @Override
    public Object getObject() throws Exception {
        log.info("生成 interface标签配置接口的代理类");

        // 获取 rpc调用时协议的具体实例对象
        if (protocol != null && !"".equals(protocol)) {
            invoke = invokes.get(protocol);
        } else {
            Protocol pro = application.getBean(Protocol.class);
            if (pro != null) {
                invoke = invokes.get(pro.getName());
            } else {
                // 默认使用的协议
                invoke = invokes.get("http");
            }
        }

        //返回代理类
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class<?>[]{Class.forName(intf)},//要代理的接口
                new InvokeInvocationHandler(invoke, this));
    }

    /**
     * 代理对象的类型
     *
     * @return
     */
    @Override
    public Class getObjectType() {
        try {
            //intf 是 reference 标签 中的 interface 属性值
            if (intf != null && !"".equals(intf)) {
                return Class.forName(intf);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * true: 代表返回的代理实例是个单例的
     *
     * @return
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        Reference.application = applicationContext;
    }

    public static ApplicationContext getApplication() {
        return application;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //获取服务的注册信息, 为了简单，所以将id和 service配置的ref一致
        registryInfo = BaseRegistryDelegate.getRegistry(id, application);
        log.info("消费端获取服务的注册信息:{}", registryInfo);


        //redis 订阅消息，即接受新的节点信息
        // RedisApi.subsribe("channel" + id, new RedisServerRegistry());
    }

    public static Map<String, Cluster> getClusters() {
        return clusters;
    }

    public List<String> getRegistryInfo() {
        return registryInfo;
    }

    public void setRegistryInfo(List<String> registryInfo) {
        this.registryInfo = registryInfo;
    }

    public Reference() {
        log.info("消费端 Reference 构造函数初始化");
    }

    public String getIntf() {
        return intf;
    }

    public void setIntf(String intf) {
        this.intf = intf;
    }

    public String getLoadbalance() {
        return loadbalance;
    }

    public void setLoadbalance(String loadbalance) {
        this.loadbalance = loadbalance;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public static Map<String, LoadBalance> getLoadBalances() {
        return loadBalances;
    }

    public static void setLoadBalances(Map<String, LoadBalance> loadBalances) {
        Reference.loadBalances = loadBalances;
    }

    public Invoke getInvoke() {
        return invoke;
    }

    public void setInvoke(Invoke invoke) {
        this.invoke = invoke;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getRetries() {
        return retries;
    }

    public void setRetries(String retries) {
        this.retries = retries;
    }
}
