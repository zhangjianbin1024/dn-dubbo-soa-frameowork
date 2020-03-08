package com.dongnao.jack.cluster;

import com.dongnao.jack.invoke.Invocation;

/**
 * 集群容错
 */
public interface Cluster {
    String invoke(Invocation invocation) throws Exception;
}
