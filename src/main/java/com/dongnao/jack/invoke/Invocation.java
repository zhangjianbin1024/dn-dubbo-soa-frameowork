package com.dongnao.jack.invoke;

import com.dongnao.jack.configBean.Reference;

import java.lang.reflect.Method;

/**
 * 代理类相关的参数
 */
public class Invocation {

    private Method method;

    private Object[] objs;

    //因为这个类中有服务的注册信息
    private Reference reference;

    //调用者，为集群容错策略中使用
    private Invoke invoke;

    public Invoke getInvoke() {
        return invoke;
    }

    public void setInvoke(Invoke invoke) {
        this.invoke = invoke;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getObjs() {
        return objs;
    }

    public void setObjs(Object[] objs) {
        this.objs = objs;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

}
