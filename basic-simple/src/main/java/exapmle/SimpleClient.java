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



        // 二、声明 交换机/队列， 并绑定路由
        String group = "example.";
        String exchangeName = group + "user";
        String queueName = group + "register";
        String routingKey = "register";
        // 声明交换机
        channel.exchangeDeclare(exchangeName, "direct", true);
        // 声明队列 exclusive = true时， 仅限本连接监听该队列
        channel.queueDeclare(queueName, true, false, true, null);
        // 绑定队列和交换机
        channel.queueBind(queueName, exchangeName, routingKey);



        // 三、发布消息
        channel.basicPublish(exchangeName, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, "{\"user\": \"光头强\"}".getBytes());
        log.info("basicPublish -- 发布用户注册消息成功");

        // 四、接收消息
        boolean autoAck = false;
        channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String routingKey = envelope.getRoutingKey();
                String contentType = properties.getContentType();
                log.info("basicConsume -- 收到用户注册消息, consumerTag={}, routingKey={}, body={}", consumerTag, routingKey, new String(body));

                // 成功
                channel.basicAck(envelope.getDeliveryTag(), false);
                // 拒绝， 第二个参数表示是否将消息放回队列
                // channel.basicReject(envelope.getDeliveryTag(), false);
                log.info("应答完成 -- 用户注册成功");
            }
        });

        System.in.read();
    }
}
