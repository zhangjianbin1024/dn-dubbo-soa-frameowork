package com.dongnao.jack.loadbalance;

import java.util.List;

/**
 * 负载均衡算法
 */
public interface LoadBalance {
    NodeInfo doSelect(List<String> registryInfo);
}
