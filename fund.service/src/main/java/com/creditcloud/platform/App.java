package com.creditcloud.platform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;



/**
 * Main class
 *
 */
@SpringBootApplication
public class App extends SpringBootServletInitializer 
{
	private static void testMysqlConnection() {
		String url = "jdbc:mysql://118.192.77.145:4040/cr_data" ;    
		 String username = "dev" ;   
		 String password = "135@$^qwe" ;   
		 try{   
		    Connection con =   DriverManager.getConnection( url, username, password);
		 }catch(SQLException se){   
			    System.out.println("数据库连接失败！");   
			    se.printStackTrace() ;   
		 }
		 System.out.println("数据库链接成功");
	}
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) 
	{
		return application.sources(App.class);
    }
    public static void main( String[] args )
    {
    	//testMysqlConnection();
        System.setProperty("spring.profiles.default", System.getProperty("spring.profiles.default", "dev"));
		final ApplicationContext applicationContext = SpringApplication.run(App.class, args);
    }
}
