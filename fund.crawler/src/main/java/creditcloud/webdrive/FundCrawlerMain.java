package creditcloud.webdrive;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import creditcloud.webdrive.crawl.DongFangFoundCrawler;
import creditcloud.webdrive.crawl.DongFangFundCrawler2;
import creditcloud.webdrive.crawl.FoundPageCrawlTask;
import creditcloud.webdrive.crawl.YuEBaoFoundCrawler;
import creditcloud.webdrive.crawl.ZhongHangFoundCrawler;
import creditcloud.webdrive.model.CreditFund;
import creditcloud.webdrive.util.RabbitMQManager;

public class FundCrawlerMain {
	static boolean bCycle = false;
	static long	   uCycleSeconds;
	static boolean  bClocks = false;
	static String[] lClocks = null;	
	public static Properties p = new Properties();
	public static String   dbConnStr = null;
	public static String   dbUserStr = null;
	public static String   dbPassStr = null;
	
	public static void testRabbitMQ( ) {
		RabbitTemplate template = RabbitMQManager.getRabbitTemplate();
		//{ "id": "270014", "name": "广发货币B", "day": "2015-01-20", "rate_10k": "1.1776", "rate_7d": "4.3960" }
		String id="270014";  String name="广发货币B"; String dStr = "2015-01-20"; 
		float rate_10k = (float) 1.1776;  float rate_7d = (float) 4.3960;
		
		String msg = "{\"id\": \"" + id + "\", \"name\": \"" + name +"\", \"day\": \""+dStr+
				"\", \"rate_10k\": "+rate_10k+"\", \"rate_7d\": \""+ rate_7d +"\" }";
		System.out.println("Message: "+msg);
		template.convertAndSend(msg);
		//template.convertAndSend("spring-boot-exchange","trust_daily_earning","hello from spring-rabbit!");
	}
	public static void main(String[] args) {
		//. initialize the rabbit context...
		RabbitMQManager.createInstance();
		//testRabbitMQ();
		bCycle = true;
		uCycleSeconds = 1800;
		//1. parse the arguments.
		for( int i=1; i<args.length; ++i ){
			//cycle
			if( args[i].equalsIgnoreCase("-c") ) {
				++i;
				uCycleSeconds = Long.parseLong(args[i]); 
				bCycle = true;
			}
			//important time points
			else if( args[i].equalsIgnoreCase("-t") ) {
				++i;
				lClocks = args[i].split(",");
				bClocks = true;
			}
			//help message
			else if( args[i].equalsIgnoreCase("-h") ) {
				System.out.println(args[0]+": -c <cycle seconds> -t <clock1,clock2...clockN>");
				return;
			}
		}
		// Load Properties
		try {
			InputStream inputStream = new FileInputStream( System.getProperty("user.dir") + File.separator + "config.properties");
			p.load(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// OPEN DB Connection
		dbConnStr = "jdbc:mysql://" + p.getProperty("ip") + ":"
				+ p.getProperty("port") + "/" + p.getProperty("db")
				+ "?useUnicode=true&characterEncoding=utf8&autoReconnect=true";
		dbUserStr = p.getProperty("user");
		dbPassStr = p.getProperty("passwd");
		
		System.out.println("jdbc:mysql://" + p.getProperty("ip") + ":"
				+ p.getProperty("port") + "/" + p.getProperty("db")
				+ "?useUnicode=true&characterEncoding=utf8&autoReconnect=true");
		
		Base.open("com.mysql.jdbc.Driver", dbConnStr,
				p.getProperty("user"), p.getProperty("passwd"));
	
		//schedule configuration
		String strCycle = p.getProperty("schedule.cycle");
		if(strCycle != null )
			uCycleSeconds = Integer.parseInt(strCycle);
		
		//history data crawling configuration..
		
		LazyList<CreditFund> founds = CreditFund.findAll();
		CreditFund.initFounds(founds);
		Base.close();		
		
		//2. initialize the crawlers
		Timer timers[] = new Timer[3];
		timers[0]=new Timer();
		timers[1]=new Timer();
		timers[2]=new Timer();
		FoundPageCrawlTask task1 = new ZhongHangFoundCrawler(  1, "ZH01", "http://www.avictc.com/prod_special_column/historical_net.html" );
		FoundPageCrawlTask task2 = new YuEBaoFoundCrawler(  2, "000198", "http://www.thfund.com.cn/website/funds/fundnet.jsp?fundcode=000198&channelid=2&categoryid=2435&childcategoryid=2438&pageno=0" );
		//FoundPageCrawlTask task3 = new DongFangFoundCrawler( "http://quote.eastmoney.com/center/fundlist.html#1,0_4" );
		FoundPageCrawlTask task3 = new DongFangFundCrawler2( "http://hq2data.eastmoney.com/fund/fundlist.aspx?jsName=fundListObj&fund=1&type=0&page=" );

		//3. call time to schedule tasks
		if( bCycle ){
			System.out.println("Enter into cycle scheduling!");
			timers[0].schedule( task1, 0, uCycleSeconds*1000 );
			timers[1].schedule( task2, 30, uCycleSeconds*1000 );
			timers[2].schedule( task3, 60, uCycleSeconds*1000 );
		}
		else if( bClocks ) {
			Calendar cal1 = Calendar.getInstance(); 
			Calendar cal2 = Calendar.getInstance(); 
			Date now = new Date();
			cal1.setTime(now);
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
			for(String clock:lClocks ) {
				try {
					cal2.setTime( formatter.parse(clock) );
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if( cal2.before(cal1 ) ) {
					cal2.add(Calendar.DAY_OF_MONTH, 1);
				}
				long delay = cal2.getTimeInMillis()-cal1.getTimeInMillis();
				timers[0].scheduleAtFixedRate( task1, delay, uCycleSeconds*1000 );
				timers[1].scheduleAtFixedRate( task2, delay, uCycleSeconds*1000 );
				timers[2].scheduleAtFixedRate( task3, delay, uCycleSeconds*1000 );
			}//!for
		}
		else
		{
			timers[0].schedule( task1, 0 );
			timers[1].schedule( task2, 0 );
			timers[2].schedule( task3, 0 );
		}
		
		while(true) {
			try {
				Thread.sleep(300000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		BlockingQueue<Integer> tasks = new LinkedBlockingQueue<Integer>();
		try {
			tasks.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//System.out.println("FundCrawler quit...");
	}
}
