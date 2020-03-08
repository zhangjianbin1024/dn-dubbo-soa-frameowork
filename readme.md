### 分析dubbo 解析原理
#### dubbo框架是如何跟spring进行整合的?
1. 定义标签 
2. 解析标标签
3. 见包 `com.dongnao.jack.spring.parse`

#### 消费者获取的代理实例是如何创建的？
1. com.dongnao.jack.configBean.Reference实现 FactoryBean 是因为要为配置的接口实现动态代理
2. com.dongnao.jack.proxy.advice.InvokeInvocationHandler 是proxy代理实例的调用处理程序
3. 见测试类 com.dongnao.jack.spring.parse.Test 测试消费端 reference
4. 生产者在启动的时候，会将信息注册到注册中心上，见：com.dongnao.jack.configBean.Service
5. 注册信息委托类：见 com.dongnao.jack.registry.BaseRegistryDelegate#registry
6. redis 注册中心的实现，见: com.dongnao.jack.registry.RedisRegistry#registry
```json
{
	"testServiceImpl": [{
		"127.0.0.1:27017": {
			"protocol": "{\"host\":\"127.0.0.1\",\"name\":\"http\",\"port\":\"27017\"}",
			"service": "{\"intf\":\"com.dongnao.jack.test.service.TestService\",\"protocol\":\"http\",\"ref\":\"testServiceImpl\"}"
		}
	}]

}
```

7 消费都向注册中心上获取服务信息，通过 reference 标签配置的id 为key获取redis上的信息，见 com.dongnao.jack.configBean.Reference#afterPropertiesSet,获取到的格式如下；

```json
{
	"127.0.0.1:27017": {
		"protocol": "{\"host\":\"127.0.0.1\",\"name\":\"http\",\"port\":\"27017\"}",
		"service": "{\"intf\":\"com.dongnao.jack.test.service.TestService\",\"protocol\":\"http\",\"ref\":\"testServiceImpl3\"}"
	}
}
```

#### 调用协议和负载均衡
> 通讯协议都在 com.dongnao.jack.configBean.Protocol 初始化 

8. 消费端 http 协议调用
    1. com.dongnao.jack.proxy.advice.InvokeInvocationHandler#invoke
    2. http 协议调用实现: com.dongnao.jack.invoke.HttpInvoke#invoke
    3. 将获取到的服务列表，通过负载均衡算法选择其中一个,见 com.dongnao.jack.loadbalance.LoadBalance#doSelect 实现类
    4. 调用http请求
    5. 生产者端，通过 com.dongnao.jack.remote.servlet.DispatcherServlet 接收请求,处理后，并返回执行结果
9. 消费端 rmi 协议调用
   1. 见 com.dongnao.jack.rmi   
   2. rmi 服务端和 rmi 客户端：com.dongnao.jack.rmi.RmiUtil
   3. rmi 协议调用实现 com.dongnao.jack.invoke.RmiInvoke
10. 消费端 netty 协议调用    
   1. com.dongnao.jack.netty.NettyUtil
   2. netty 服务端启动后，netty客户端连接，netty 客户端发送消息到服务端时，会进入 com.dongnao.jack.netty.NettyServerInHandler#channelRead
   3. 客户雄端发送消息和接受服务端的消息： com.dongnao.jack.netty.NettyClientInHandler

#### 集群容错
11. 集群容错
   - failover 自动切换到好的机器上
   - failfast 如果调用节点失败，则直接就失败
   - failsafe 如果调用节点失败，就忽略

#### 服务发现与剔除功能
12. 服务发现与剔除功能 
    1. 节点调用不通时，可以将该节点剔除掉
    2. 节点新增时，将节点添加到消费都的服务列表容器中，见 com.dongnao.jack.configBean.Reference#registryInfo    
13. 服务发现功能
   0. 通过redis的发布和订阅功能，完成新的服务节点添加到 com.dongnao.jack.configBean.Reference#registryInfo 容器中
   1. com.dongnao.jack.redis.RedisServerRegistry
   2. 发布节点消息 com.dongnao.jack.configBean.Service.afterPropertiesSet
   3. 订阅节点消息 com.dongnao.jack.configBean.Reference.afterPropertiesSet     