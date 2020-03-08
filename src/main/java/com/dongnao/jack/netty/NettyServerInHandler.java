package com.dongnao.jack.netty;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dongnao.jack.configBean.Service;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NettyServerInHandler extends ChannelInboundHandlerAdapter {

    /**
     * netty的客户端有消息过来的时候就会调到这个方法
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        ByteBuf result = (ByteBuf) msg;
        byte[] result1 = new byte[result.readableBytes()];
        result.readBytes(result1);
        String resultStr = new String(result1);
        System.out.println(resultStr);

        result.release();

        //将服务端执行的结果返回给客户端
        String response = invokeService(resultStr);

        ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
        encoded.writeBytes(response.getBytes());
        ctx.writeAndFlush(encoded);
        ctx.close();
    }

    private String invokeService(String param) {
        JSONObject requestparam = JSONObject.parseObject(param);
        //要从远程的生产者的spring容器中拿到对应的serviceid实例
        String serviceId = requestparam.getString("serviceId");
        String methodName = requestparam.getString("methodName");
        JSONArray paramTypes = requestparam.getJSONArray("paramTypes");
        //这个对应的方法参数
        JSONArray methodParamJa = requestparam.getJSONArray("methodParams");
        //这个就是反射的参数
        Object[] objs = null;
        if (methodParamJa != null) {
            objs = new Object[methodParamJa.size()];
            int i = 0;
            for (Object o : methodParamJa) {
                objs[i++] = o;
            }
        }

        //拿到spring的上下文
        ApplicationContext application = Service.getApplication();
        //服务层的实例
        Object serviceBean = application.getBean(serviceId);

        //这个方法的获取，要考虑到这个方法的重载
        Method method = getMethod(serviceBean, methodName, paramTypes);

        if (method != null) {

            Object result;
            try {
                result = method.invoke(serviceBean, objs);
                return result.toString();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        } else {
            return "---------------------------------nosuchmethod-----------------------------";
        }
        return null;
    }

    private Method getMethod(Object bean, String methodName,
                             JSONArray paramTypes) {

        Method[] methods = bean.getClass().getMethods();
        List<Method> retMethod = new ArrayList<Method>();

        for (Method method : methods) {
            //把名字和methodName入参相同的方法加入到list中来
            if (methodName.trim().equals(method.getName())) {
                retMethod.add(method);
            }
        }

        //如果大小是1就说明相同的方法只有一个
        if (retMethod.size() == 1) {
            return retMethod.get(0);
        }

        boolean isSameSize = false;
        boolean isSameType = false;
        jack:
        for (Method method : retMethod) {
            Class<?>[] types = method.getParameterTypes();

            if (types.length == paramTypes.size()) {
                isSameSize = true;
            }

            if (!isSameSize) {
                continue;
            }

            for (int i = 0; i < types.length; i++) {
                if (types[i].toString().contains(paramTypes.getString(i))) {
                    isSameType = true;
                } else {
                    isSameType = false;
                }
                if (!isSameType) {
                    continue jack;
                }
            }

            if (isSameType) {
                return method;
            }
        }
        return null;
    }

}
