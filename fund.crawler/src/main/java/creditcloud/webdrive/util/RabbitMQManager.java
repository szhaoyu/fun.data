package creditcloud.webdrive.util;

import java.io.File;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class RabbitMQManager {
	private static AbstractApplicationContext ctx = null;

	public static void createInstance() {
		ctx = new ClassPathXmlApplicationContext("applicationContext-rabbitmq.xml");
		//ctx = new FileSystemXmlApplicationContext(System.getProperty("user.dir") + File.separator+"applicationContext-amqp.xml");
	}
	synchronized public static RabbitTemplate getRabbitTemplate() {
		return ctx.getBean(RabbitTemplate.class);
	}
}
