package com.dongnao.jack.registry;

import com.alibaba.fastjson.JSONObject;
import com.dongnao.jack.configBean.Protocol;
import com.dongnao.jack.configBean.Registry;
import com.dongnao.jack.configBean.Service;
import com.dongnao.jack.redis.RedisApi;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description 这个是redis的注册中心处理类
 * @ClassName RedisRegistry
 * @Date 2017年11月14日 下午8:54:40
 * @Author dn-jack
 */

public class RedisRegistry implements BaseRegistry {

    @Override
    public boolean registry(String ref, ApplicationContext application) {
        try {
            // 注册的协议
            Protocol protocol = application.getBean(Protocol.class);
            // 获取所有配置的 service 标签bean
            Map<String, Service> services = application.getBeansOfType(Service.class);

            //获取 注册中心
            Registry registry = application.getBean(Registry.class);
            RedisApi.createJedisPool(registry.getAddress());

            // 向注册中心，注册信息
            for (Map.Entry<String, Service> entry : services.entrySet()) {
                if (entry.getValue().getRef().equals(ref)) {
                    JSONObject jo = new JSONObject();
                    jo.put("protocol", JSONObject.toJSONString(protocol));
                    jo.put("service", JSONObject.toJSONString(entry.getValue()));

                    JSONObject ipport = new JSONObject();
                    ipport.put(protocol.getHost() + ":" + protocol.getPort(), jo);
                    //信息写入redis
                    lpush(ipport, ref);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param ipport 提供服务的ip和端口信息
     * @param key    服务名 id
     */
    private void lpush(JSONObject ipport, String key) {
        if (RedisApi.exists(key)) {
            Set<String> keys = ipport.keySet();
            String ipportStr = "";
            //这个循环里面只会循环一次,
            for (String kk : keys) {
                ipportStr = kk;
            }

            //拿redis对应key：host:端口号 里面的内容
            List<String> registryInfo = RedisApi.lrange(key);
            List<String> newRegistry = new ArrayList<String>();

            boolean isold = false;

            for (String node : registryInfo) {
                JSONObject jo = JSONObject.parseObject(node);
                //注册中心上有该ip和端口信息
                if (jo.containsKey(ipportStr)) {
                    newRegistry.add(ipport.toJSONString());
                    isold = true;
                } else {
                    newRegistry.add(node);
                }
            }

            if (isold) {
                //这里是老机器启动去重
                if (newRegistry.size() > 0) {
                    //删除所有注册信息
                    RedisApi.del(key);
                    //重新注册所有信息
                    String[] newReStr = new String[newRegistry.size()];
                    for (int i = 0; i < newRegistry.size(); i++) {
                        newReStr[i] = newRegistry.get(i);
                    }
                    RedisApi.lpush(key, newReStr);
                }
            } else {
                //这里是加入新启动的机器
                RedisApi.lpush(key, ipport.toJSONString());
            }
        } else {
            //所有的都是第一次啟動
            RedisApi.lpush(key, ipport.toJSONString());
        }
    }

    @Override
    public List<String> getRegistry(String id, ApplicationContext application) {
        try {
            Registry registry = application.getBean(Registry.class);
            RedisApi.createJedisPool(registry.getAddress());
            if (RedisApi.exists(id)) {
                //拿key 对应的list，也就是服务列表信息
                return RedisApi.lrange(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
