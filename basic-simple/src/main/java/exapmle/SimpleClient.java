package exapmle;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * 最简单的RabbitMQ客户端
 * 官方示例： http://www.rabbitmq.com/api-guide.html#connecting
 */
public class SimpleClient {

    private static final Logger log = LoggerFactory.getLogger(SimpleClient.class);

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException, IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        // 一、连接工厂配置
        // 方式1, 以下均是使用的rabbitMQ默认值， 可以不用设置
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");
        factory.setHost("localhost");
        factory.setPort(5672);

        // 方式2  使用URI配置:
        // factory.setUri("amqp://userName:password@hostName:portNumber/virtualHost");

        Connection conn = factory.newConnection();
        log.info("连接RabbitMQ服务成功");

        final Channel channel = conn.createChannel();
        log.info("打开channel成功");

        // 队列消息示例
        // queueExample(conn, channel);

        // Topic消息示例
        topicExample(conn, channel);
        System.in.read();
    }

    /**
     * 演示队列
     */
    private static void queueExample(Connection conn, Channel channel) throws IOException {
        // 二、声明 交换机/队列， 并绑定路由
        String exchangeName = "userDirect";
        String queueName = "register";
        String routingKey = exchangeName + "." + queueName;
        // 声明交换机
        channel.exchangeDeclare(exchangeName, "direct", true);
        // 声明队列 exclusive = true时， 仅限本连接监听该队列
        channel.queueDeclare(queueName, true, false, true, null);
        // 绑定队列和交换机
        channel.queueBind(queueName, exchangeName, routingKey);



        // 三、发布消息
        for(int i = 0; i < 1000; i ++){
            channel.basicPublish(exchangeName, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, ("{\"user\": \"光头强" + i + "\"}").getBytes());
            log.info("basicPublish -- 发布用户注册消息成功");
        }

        // 四、接收消息
        boolean autoAck = false;
        channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {
            int i = 0;
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String routingKey = envelope.getRoutingKey();
                log.info("basicConsume -- 收到用户注册消息, consumerTag={}, routingKey={}, body={}", consumerTag, routingKey, new String(body));

                // 应答方式：
                //  1、可以全都用basicAck， 异常情况再做补偿
                //  2、对于第三方超时可以用basicReject、basicNack, requeue传true, 但需要控制重试次数, 多次仍然失败后记错误日志并将requeue传false, 丢弃消息
                //  3、非超时异常用basicReject、basicNack, requeue传false, 不要将消息重新入队, 记录日志, 后续走补偿修复

                boolean multiple = true;// 表示批量， true时应答deliveryTag <= envelope.getDeliveryTag()之前的所有消息
                boolean requeue = false; // 是否重新入队
                if((i % 5) == 0) {
                    // 成功, 可支持批量
                    channel.basicAck(envelope.getDeliveryTag(), multiple);
                } else if((i % 8) == 0) {
                    // 拒绝, 不支持批量
                    channel.basicReject(envelope.getDeliveryTag(), requeue);
                } else {
                    // 否定, 可支持批量
                    channel.basicNack(envelope.getDeliveryTag(), multiple, requeue);
                    log.info("应答完成 -- 用户注册成功");
                }
            }
        });
    }   // End queueExample

    /**
     * 演示发布/订阅， 如用户注册时， 分别为用户初始化账户、个人信息、其他信息
     */
    private static void topicExample(Connection conn, Channel channel) throws IOException {
        // 二、声明 交换机/队列， 并绑定路由
        String exchangeName = "userTopic";
        // 声明交换机
        channel.exchangeDeclare(exchangeName, "topic", true);

        // 设置多个监听器
        String[] queueNames = new String[] {"initAccount", "initInfo", "initOther"};
        // 三、监听消息
        for(final String queueName: queueNames) {
            // 声明队列
            channel.queueDeclare(queueName, true, false, false, null);
            // 绑定队列和交换机
            channel.queueBind(queueName, exchangeName, "userTopic.*");

            boolean autoAck = false;
            channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String routingKey = envelope.getRoutingKey();
                    log.info("queue={} -- 收到用户注册消息, consumerTag={}, routingKey={}, body={}", queueName, consumerTag, routingKey, new String(body));

                    // 成功
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            });

        }

        // 三、发布一条消息， 将匹配路由userTopic.*
        channel.basicPublish(exchangeName, "userTopic.register", MessageProperties.PERSISTENT_TEXT_PLAIN, "{\"user\": \"光头强注册啦\", \"ip\": \"127.0.0.1\"}".getBytes());
        log.info("basicPublish -- 发布光头强注册消息成功");

    }   // End topicExample

}
