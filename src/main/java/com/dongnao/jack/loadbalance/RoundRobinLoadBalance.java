package com.dongnao.jack.loadbalance;

import com.alibaba.fastjson.JSONObject;

import java.util.Collection;
import java.util.List;

/**
 * @Description 轮询的负载均衡算法
 * @ClassName RoundRobinLoadBalance
 * @Date 2017年11月14日 下午10:25:41
 * @Author dn-jack
 */

public class RoundRobinLoadBalance implements LoadBalance {

    //有线程安全问题，所以要用同步块
    private static Integer index = 0;

    @Override
    public NodeInfo doSelect(List<String> registryInfo) {
        synchronized (index) {
            if (index >= registryInfo.size()) {
                index = 0;
            }
            String registry = registryInfo.get(index);
            index++;
            JSONObject registryJo = JSONObject.parseObject(registry);
            Collection values = registryJo.values();
            JSONObject node = new JSONObject();
            for (Object value : values) {
                node = JSONObject.parseObject(value.toString());
            }

            JSONObject protocol = node.getJSONObject("protocol");
            NodeInfo nodeinfo = new NodeInfo();
            nodeinfo.setHost(protocol.get("host") != null ? protocol.getString("host")
                    : "");
            nodeinfo.setPort(protocol.get("port") != null ? protocol.getString("port")
                    : "");
            nodeinfo.setContextpath(protocol.get("contextpath") != null ? protocol.getString("contextpath")
                    : "");
            return nodeinfo;
        }
    }
}
