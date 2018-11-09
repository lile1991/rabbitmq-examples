# RabbitMQ Example
   #### rabbitmq客户端示例:
   基于amqp-client的客户端示例: example.simple.SimpleClient   
   官方示例文档:   
        http://www.rabbitmq.com/api-guide.html  
        http://www.rabbitmq.com/getstarted.html  
   输出示例：
   ```
   [main] INFO exapmle.SimpleClient - 连接RabbitMQ服务成功
   [main] INFO exapmle.SimpleClient - 打开channel成功
   [main] INFO exapmle.SimpleClient - basicPublish -- 发布用户注册消息成功
   [pool-1-thread-4] INFO exapmle.SimpleClient - basicConsume -- 收到用户注册消息, consumerTag=amq.ctag-wmcRjeqRW3ZxrKG2bRvX1w, routingKey=register, body={"user": "光头强"}
   [pool-1-thread-4] INFO exapmle.SimpleClient - 用户注册成功
   ```
     
RabbitMQ应答方式说明：
1. 可以全都用basicAck， 异常情况再做补偿
2. 对于第三方超时可以用basicReject、basicNack, requeue传true, 但需要控制重试次数, 多次仍然失败后记错误日志并将requeue传false, 丢弃消息
3. 非超时异常用basicReject、basicNack, requeue传false, 不要将消息重新入队, 记录日志, 后续走补偿修复