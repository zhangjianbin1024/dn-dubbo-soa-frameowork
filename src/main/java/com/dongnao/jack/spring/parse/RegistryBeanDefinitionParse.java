package com.dongnao.jack.spring.parse;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @Description Registry标签的解析类
 * @ClassName RegistryBeanDefinitionParse
 * @Date 2017年11月11日 下午9:22:12
 * @Author dn-jack
 */

public class RegistryBeanDefinitionParse implements BeanDefinitionParser {

    //Regitry
    private Class<?> beanClass;

    public RegistryBeanDefinitionParse(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        //spring会把这个beanClass进行实例化  bean 名字缓存到 BeanDefinitionNames容器中
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);
        String protocol = element.getAttribute("protocol");
        String address = element.getAttribute("address");

        if (protocol == null || "".equals(protocol)) {
            throw new RuntimeException("Regitry protocol 不能为空！");
        }
        if (address == null || "".equals(address)) {
            throw new RuntimeException("Regitry address 不能为空！");
        }

        //对 beanClass 的属性性进行赋值
        beanDefinition.getPropertyValues().addPropertyValue("protocol",
                protocol);
        beanDefinition.getPropertyValues().addPropertyValue("address", address);

        // 将 beanDefinition 对象中的bean名字加入到 `beanDefinitionNames` 容器中。
        parserContext.getRegistry()
                .registerBeanDefinition("Registry" + address, beanDefinition);
        return beanDefinition;
    }
}
