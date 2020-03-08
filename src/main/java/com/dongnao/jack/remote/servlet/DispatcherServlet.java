package com.dongnao.jack.remote.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dongnao.jack.configBean.Service;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description 这个是soa框架中给生产者接收请求用的servlet，这个必须是采用http协议才能调得到
 * @ClassName DispatcherServlet
 * @Date 2017年11月16日 下午8:32:06
 * @Author dn-jack
 */

public class DispatcherServlet extends HttpServlet {

    /**
     * @Fields serialVersionUID TODO
     */

    private static final long serialVersionUID = 2368065256546765L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            JSONObject requestparam = httpProcess(req, resp);
            //要从远程的生产者的spring容器中拿到对应的serviceid实例
            String serviceId = requestparam.getString("serviceId");
            String methodName = requestparam.getString("methodName");
            JSONArray paramTypes = requestparam.getJSONArray("paramTypes");
            //这个对应的方法参数
            JSONArray methodParamJa = requestparam.getJSONArray("methodParams");
            //这个就是反射时方法调用的参数
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
            //服务层的实例：扩展：可以serviceId换成 接口的字符串，根据接口的字符串装换成类，根据类的类型在spring 容器中获取ben
            Object serviceBean = application.getBean(serviceId);

            //这个方法的获取，要考虑到这个方法的重载
            Method method = getMethod(serviceBean, methodName, paramTypes);

            if (method != null) {
                Object result;

                result = method.invoke(serviceBean, objs);

                PrintWriter pw = resp.getWriter();
                pw.write(result.toString());
            } else {
                //返回没有找到对应的方法
                PrintWriter pw = resp.getWriter();
                pw.write("---------------------------------nosuchmethod-----------------------------");
            }
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
            // 方法的参数类型
            Class<?>[] types = method.getParameterTypes();

            if (types.length == paramTypes.size()) {
                isSameSize = true;
            }

            if (!isSameSize) {
                continue;
            }

            for (int i = 0; i < types.length; i++) {
                //对参数的类型一个一个进行比对，看是否是要找的那个方法
                if (types[i].toString().contains(paramTypes.getString(i))) {
                    isSameType = true;
                } else {
                    isSameType = false;
                }
                if (!isSameType) {
                    continue jack;//跳到最外层，循环下一个方法，进行判断
                }
            }

            if (isSameType) {
                return method;
            }
        }
        return null;
    }

    /**
     * 获取请求中的参数
     *
     * @param req
     * @param resp
     * @return
     * @throws IOException
     */
    public static JSONObject httpProcess(HttpServletRequest req,
                                         HttpServletResponse resp) throws IOException {
        StringBuffer sb = new StringBuffer();
        InputStream is = req.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is,
                "utf-8"));
        String s = "";
        while ((s = br.readLine()) != null) {
            sb.append(s);
        }
        if (sb.toString().length() <= 0) {
            return null;
        } else {
            return JSONObject.parseObject(sb.toString());
        }
    }

}
