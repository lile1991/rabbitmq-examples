# Rabbitmq Example
#### rabbitmq客户端示例:
基于amqp-client的客户端示例: example.simple.SimpleClient, 官方示例文档: http://www.rabbitmq.com/api-guide.html  
输出示例：
```
[main] INFO exapmle.SimpleClient - 连接RabbitMQ服务成功
[main] INFO exapmle.SimpleClient - 打开channel成功
[main] INFO exapmle.SimpleClient - basicPublish -- 发布用户注册消息成功
[pool-1-thread-4] INFO exapmle.SimpleClient - basicConsume -- 收到用户注册消息, consumerTag=amq.ctag-wmcRjeqRW3ZxrKG2bRvX1w, routingKey=register, body={"user": "光头强"}
[pool-1-thread-4] INFO exapmle.SimpleClient - 用户注册成功
```