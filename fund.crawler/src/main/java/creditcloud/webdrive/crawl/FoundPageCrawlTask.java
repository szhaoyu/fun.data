package creditcloud.webdrive.crawl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import creditcloud.webdrive.model.CreditFund;
import creditcloud.webdrive.model.CreditFundPrice;
import creditcloud.webdrive.model.CreditRate;
import creditcloud.webdrive.util.RabbitMQManager;

public class FoundPageCrawlTask extends TimerTask {

	public String url;
	
	public RabbitTemplate template;
	
	public FoundPageCrawlTask( String url ) {
		this.url = url;
		this.template = RabbitMQManager.getRabbitTemplate();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
	}
	
	public void sendUpdateMessage(String id, String name, String dStr, float rate_10k, float rate_7d ) {
		//{ "id": "270014", "name": "广发货币B", "day": "2015-01-20", "rate_10k": "1.1776", "rate_7d": "4.3960" }
		String msg = "{\"id\": \"" + id + "\", \"name\": \"" + name +"\", \"day\": \""+dStr+
				"\", \"profit_10k\": "+rate_10k+"\", \"rate_7d\": \""+ rate_7d +"\" }";
		System.out.println("Message: "+msg);
		template.convertAndSend( msg );
	}
	/*
	 * Save into table credit_found_rates
	 * @return  true - already exist; false - not exist before.
	 */	
	public boolean saveFoundItem(CreditFund cf, int foundID, String dStr, float ratePer10k, float ratePerYear ){
		LazyList<CreditFundPrice> rates = CreditFundPrice.find("credit_fund_id=? and pubdate=?", foundID, dStr );
		CreditFundPrice rateItem = new CreditFundPrice();
		
		rateItem.setInteger("credit_fund_id", foundID);
		rateItem.setString("pubdate", dStr);
		rateItem.setFloat("profit_10k",  ratePer10k);
		rateItem.setFloat("rate_year", ratePerYear);
		
		//新抓取纪录，需要通知服务端计算收益率
		if( rates.size() == 0 ) {
			rateItem.insert();
			//check with Teddy
			//this.sendUpdateMessage( cf.getString("ori_id"), cf.getString("name"), dStr, ratePer10k, ratePerYear );
			System.out.println(new Date()+": insert credit_fund_id:"+foundID+", pubdate:"+dStr);
			return false;
		}else
		{
			CreditFundPrice.update("profit_10k=?,rate_year=?", "credit_fund_id="+foundID+" and pubdate='"+dStr+"'", ratePer10k, ratePerYear);
			System.out.println(new Date()+": update credit_fund_id:"+foundID+", pubdate:"+dStr);
		}
		
		return true;
	}
}
