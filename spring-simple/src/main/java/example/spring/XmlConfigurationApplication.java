package example.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class XmlConfigurationApplication {
    private static final Logger log = LoggerFactory.getLogger(XmlConfigurationApplication.class);

    public static void main(String[] args) throws IOException {
        ApplicationContext context = new ClassPathXmlApplicationContext("rabbitConfiguration.xml");
        AmqpAdmin amqpAdmin = context.getBean(AmqpAdmin.class);

        // 声明Exchange
        Exchange exchange = new DirectExchange("example.spring.xmlExchange");
        amqpAdmin.declareExchange(exchange);

        // 声明Queue
        Queue queue = new Queue("example.spring.xmlQueue");
        amqpAdmin.declareQueue(queue);

        String routingKey = "xmlRoutingKey";
        // 绑定Queue和Exchange
        // amqpAdmin.declareBinding(new Binding(queue.getName(), Binding.DestinationType.QUEUE, exchange.getName(), "xmlRoutingKey", null));
        // 或使用下面这种绑定
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs());


        AmqpTemplate amqpTemplate = context.getBean(AmqpTemplate.class);
        amqpTemplate.convertAndSend(exchange.getName(), routingKey, "光头强");
        log.info("发送消息成功");

        log.info("接收消息: {}", amqpTemplate.receiveAndConvert(queue.getName()));
        System.in.read();
    }
}
