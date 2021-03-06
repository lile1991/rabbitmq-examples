# RabbitMQ Example
   #### rabbitmq客户端示例:
   基于amqp-client的客户端示例: example.simple.SimpleClient   
   官方示例文档:   
        http://www.rabbitmq.com/api-guide.html  
        http://www.rabbitmq.com/getstarted.html  
   Queue输出示例：
   ```
   [main] INFO exapmle.SimpleClient - 连接RabbitMQ服务成功
   [main] INFO exapmle.SimpleClient - 打开channel成功
   [main] INFO exapmle.SimpleClient - basicPublish -- 发布用户注册消息成功
   [pool-1-thread-4] INFO exapmle.SimpleClient - basicConsume -- 收到用户注册消息, consumerTag=amq.ctag-wmcRjeqRW3ZxrKG2bRvX1w, routingKey=register, body={"user": "光头强"}
   [pool-1-thread-4] INFO exapmle.SimpleClient - 用户注册成功   
   ```  
     
   Topic输出示例(用户注册时， 分别为用户初始化账户[initAccount]、个人信息[initInfo]、其他信息[initOther])：
   ```
   [main] INFO exapmle.SimpleClient - 连接RabbitMQ服务成功
   [main] INFO exapmle.SimpleClient - 打开channel成功
   [main] INFO exapmle.SimpleClient - basicPublish -- 发布光头强注册消息成功
   [pool-1-thread-6] INFO exapmle.SimpleClient - queue=initAccount -- 收到用户注册消息, consumerTag=amq.ctag-bjEWeOYFd2zh3eTzWqUNOg, routingKey=userTopic.register, body={"user": "光头强注册啦", "ip": "127.0.0.1"}
   [pool-1-thread-6] INFO exapmle.SimpleClient - queue=initInfo -- 收到用户注册消息, consumerTag=amq.ctag-awMfU-Q1gearAtcRAi039Q, routingKey=userTopic.register, body={"user": "光头强注册啦", "ip": "127.0.0.1"}
   [pool-1-thread-6] INFO exapmle.SimpleClient - queue=initOther -- 收到用户注册消息, consumerTag=amq.ctag-_CiASYmCWIuD_z5ESXLNLQ, routingKey=userTopic.register, body={"user": "光头强注册啦", "ip": "127.0.0.1"}
   ```
     
RabbitMQ应答方式说明：
1. 可以全都用basicAck， 异常情况再做补偿
2. 对于第三方超时可以用basicReject、basicNack, requeue传true, 但需要控制重试次数, 多次仍然失败后记录数据，并将requeue传false, 后续补偿修复
3. 非超时异常用basicReject、basicNack, requeue传false, 不要将消息重新入队, 记录数据, 后续走补偿修复