package creditcloud.webdrive.crawl;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

import creditcloud.webdrive.FundCrawlerMain;
import creditcloud.webdrive.model.CreditFund;


//http://hq2data.eastmoney.com/fund/fundlist.aspx?jsName=fundListObj&fund=0&type=0&page=
public class DongFangFundCrawler2 extends FoundPageCrawlTask {
	private int 	pageSize = 0 ;
	private int 	pageNum = 1;
	private int		retryLimit = 3;
	
	public DongFangFundCrawler2(String url) {
		super(url);
		// TODO Auto-generated constructor stub
	}
	
	private int  procItems( String str ) {
		Pattern date_pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");		
		Pattern record_pattern = Pattern.compile("<tr>.+?</tr>");
		
		Pattern head_pattern = Pattern.compile("<td colspan=\"2\">.*?</td>");
		Pattern td_pattern = Pattern.compile("<td>.*?</td>");

		String curDate = null, preDate = null;
		
		Matcher m3 = record_pattern.matcher(str);
		//for each record
		int count=0;
        String foundCode = null;
        String foundName = null;
        String rate_w1 = null;
        String rate_w2 = null;
        String rate_year1 = null;
        String rate_year2 = null;
        String boot_day = null;
        String manager = null;
		while( m3.find() ) {
			String item = m3.group();
			//时间
			Matcher m1 = head_pattern.matcher(item);
			if( m1.find() ) {	//<td rowspan="2">序号</td>
				//today
				curDate = m1.group();
				curDate = curDate.replaceAll("<[^>]+?>", "");
				//yesterday
				m1.find();
				preDate = m1.group();
				preDate = preDate.replaceAll("<[^>]+?>", "");
				
				continue;
			}
			
			//字段值域名
			Matcher m4 = td_pattern.matcher(item);
			//column 1
			if( !m4.find() )
				continue;
			//column 2
			m4.find();
			foundCode = m4.group();
			foundCode = foundCode.replaceAll("<[^>]+?>", "");
			//column 3
			m4.find();
			foundName = m4.group();
			foundName = foundName.replaceAll("<[^>]+?>", "");
			//rate w 1
			m4.find();
			rate_w1 = m4.group();
			rate_w1 = rate_w1.replaceAll("<[^>]+?>", "");
			rate_w1 = rate_w1.trim();
			//rate year1
			m4.find();
			rate_year1 = m4.group(); rate_year1 = rate_year1.replaceAll("<[^>]+?>", "");  rate_year1 = rate_year1.replace('%', ' '); rate_year1 = rate_year1.trim();
			//rate w 2
			m4.find();
			rate_w2 = m4.group(); 
			rate_w2 = rate_w2.replaceAll("<[^>]+?>", "");
			rate_w2 = rate_w2.trim();
			//rate year2
			m4.find();
			rate_year2 = m4.group(); rate_year2 = rate_year2.replaceAll("<[^>]+?>", "");	rate_year2 = rate_year2.replace('%', ' '); rate_year2=rate_year2.trim();
			//boot day
			m4.find();
			boot_day = m4.group();
			boot_day = boot_day.replaceAll("<[^>]+?>", "");
			//manager
			m4.find();
			manager = m4.group();
			manager = manager.replaceAll("<[^>]+?>", "");

			//save found item
        	boolean insert = false;
        	CreditFund cf = CreditFund.getFoundByOrgID( foundCode );
        	if( cf == null ){
        		cf = new CreditFund();
        		cf.setString("ori_id", foundCode );
        		insert = true;
        	}
    		cf.setString("name", foundName );
    		cf.setString("boot_day", boot_day );
    		cf.setString("manager", manager );
    		cf.setString("scan_time", curDate );
    		CreditFund.setFoundItem(cf);
    		if(insert )
    			cf.insert();
    		else
    			cf.save();
        	//save rate item
    		LazyList<CreditFund> cfs = CreditFund.find("ori_id=?", foundCode);
    		int foundID = 0;
    		if( cfs.size() > 0 )
    				foundID = cfs.get(0).getInteger("id");
    		
    		this.saveFoundItem(cf, foundID, curDate, 
    				(rate_w1==null||rate_w1.isEmpty())?0:Float.parseFloat(rate_w1), 
    				(rate_year1==null||rate_year1.isEmpty())?0:Float.parseFloat(rate_year1));
    		this.saveFoundItem(cf, foundID, preDate, 
    				(rate_w2==null||rate_w2.isEmpty())?0:Float.parseFloat(rate_w2), 
    				(rate_year2==null||rate_year2.isEmpty())?0:Float.parseFloat(rate_year2));
    		++count;
		}//!while
		
		return count;
	}
	
	@Override
	public void run() {
		System.out.println(new Date()+":"+url);
		Base.open("com.mysql.jdbc.Driver", FundCrawlerMain.dbConnStr, FundCrawlerMain.dbUserStr, FundCrawlerMain.dbPassStr );
		CloseableHttpClient httpclient = HttpClients.createDefault();
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
             public String handleResponse( final HttpResponse response) throws ClientProtocolException, IOException {
                 int status = response.getStatusLine().getStatusCode();
                 if (status >= 200 && status < 300) {
                     HttpEntity entity = response.getEntity();
                     return entity != null ? EntityUtils.toString(entity) : null;
                 } else {
                     throw new ClientProtocolException("Unexpected response status: " + status);
                 }
             }
         };
        pageNum = 1;
        int retry = 0;
        int result = 0;
		while(true) {
			HttpGet httpget = new HttpGet(this.url + pageNum);
			String  html = null;
			try {
				html = httpclient.execute(httpget, responseHandler);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				html = null;
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				html = null;
			}
			//sleep
			try {
				Thread.sleep( 15000 );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//
			if( html == null
				|| html.contains("运行时错误") 
				|| (result = procItems(html)) == 0
					) {
				if( ++retry > this.retryLimit )
					break;
				continue;
			}
			//the last page
			if( result < pageSize )
				break;
			if( result > pageSize )
				pageSize = result;
			//move to next page...
			++pageNum;
		}
		try {
			httpclient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Base.close();
	}
}
