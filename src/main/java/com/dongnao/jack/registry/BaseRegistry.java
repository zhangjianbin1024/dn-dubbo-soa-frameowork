package com.dongnao.jack.registry;

import org.springframework.context.ApplicationContext;

import java.util.List;

public interface BaseRegistry {

    boolean registry(String param, ApplicationContext application);


    List<String> getRegistry(String id, ApplicationContext application);
}
