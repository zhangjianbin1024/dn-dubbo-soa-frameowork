package com.dongnao.jack.proxy.advice;

import com.dongnao.jack.cluster.Cluster;
import com.dongnao.jack.configBean.Reference;
import com.dongnao.jack.invoke.Invocation;
import com.dongnao.jack.invoke.Invoke;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Description InvokeInvocationHandler 这个是一个advice，在这个advice里面就进行了rpc的远程调用
 * rpc：http、rmi、dubbo、netty
 * @ClassName InvokeInvocationHandler
 * @Date 2017年11月11日 下午10:14:51
 * @Author dn-jack
 */
@Slf4j
public class InvokeInvocationHandler implements InvocationHandler {

    //具体调用过程的实现对象
    private Invoke invoke;

    private Reference reference;

    public InvokeInvocationHandler(Invoke invoke, Reference reference) {
        this.invoke = invoke;
        this.reference = reference;
    }

    /**
     * 接口中的方法被调用时，会调用到这里
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        //在这个invoke里面最终要调用多个远程的 provider
        log.info("已经获取到了代理实例，已经调到了InvokeInvocationHandler.invoke");

        Invocation invocation = new Invocation();
        invocation.setMethod(method);
        invocation.setObjs(args);
        invocation.setReference(reference);
        invocation.setInvoke(invoke);

        //调用结果 v1
        //String result = invoke.invoke(invocation);

        //获取集群容错配置
        Cluster cluster = Reference.getClusters().get(reference.getCluster());
        //调用结果 v2
        String result = cluster.invoke(invocation);
        return result;
    }
}
