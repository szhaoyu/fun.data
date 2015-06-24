package creditcloud.webdrive.crawl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

import creditcloud.webdrive.FundCrawlerMain;
import creditcloud.webdrive.model.CreditFund;

//东方财富基金
//http://quote.eastmoney.com/center/fundlist.html#1,0_4
//http://hq2data.eastmoney.com/fund/fundlist.aspx?jsName=fundListObj&fund=0&type=0&page=1
public class DongFangFoundCrawler extends FoundPageCrawlTask {

	public DongFangFoundCrawler(String url) {
		super(url);
		// TODO Auto-generated constructor stub
	}
	
	/*
	 <tr>    
	 <td>281</td>    
	 <td><a href="http://fund.eastmoney.com/730103.html" target="_blank">730103</a></td>    
	 <td><a href="http://fund.eastmoney.com/730103.html" target="_blank">方正富邦货币B</a></td>    
	 <td>1.5794</td>    
	 <td><span class="red">7.1240%</span></td>
	 <td>1.5138</td>   
	 <td><span class="red">6.5960%</span></td>    
	 <td>2012-12-26</td>    
	 <td><a href="http://fund.eastmoney.com/f10/jjjl_730103.html" target="_blank">沈毅,李文君</a></td>  
	 </tr>
	 
	 <tr class="bg-th">   
	  <td rowspan="2" style="width: 29px;">序号</td>   
	  <td rowspan="2" data-sort="bzdm" style="width: 58px;"><a>基金代码</a></td>  
	  <td rowspan="2" style="width: 143px;">基金名称</td>    
	  <td colspan="2" style="width: 197px;">2014-12-29</td>    
	  <td colspan="2" style="width: 183px;">2014-12-26</td>    
	  <td rowspan="2" style="width: 79px;">基金成立日期</td>    
	  <td rowspan="2" style="width: 84px;">基金经理</td>  
	  </tr>
	 */

	private void  procItems( String str ) {
		Pattern date_pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
		//Pattern head_pattern=Pattern.compile("<tr class=\"bg-th\">\\s+?<td rowspan=\"2\" style=\"width: 29px;\">序号</td>\\s+<td rowspan=\"2\" data-sort=\"bzdm\" style=\"width: 58px;\"><a>基金代码</a></td>\\s+"
	//+"<td rowspan=\"2\" style=\"width: 143px;\">基金名称</td>\\s+<td colspan=\"2\" style=\"width: 197px;\">"
	//+"\\d{4}-\\d{2}-\\d{2}</td>\\s+?<td colspan=\"2\" style=\"width: 183px;\">\\d{4}-\\d{2}-\\d{2}</td>\\s+?<td rowspan=\"2\" style=\"width: 79px;\">基金成立日期</td>\\s+?<td rowspan=\"2\" style=\"width: 84px;\">基金经理</td>\\s+?</tr>");
		Pattern head_pattern=Pattern.compile("<tr class=\"bg-th\">.+?</tr>");
		Matcher m1 = head_pattern.matcher(str);
		
		String curDate = null, preDate = null;
		if( m1.find() ){
			String head = m1.group();
			Matcher m2 = date_pattern.matcher(head);
			if( m2.find() )
				curDate = m2.group();
			if( m2.find())
				preDate = m2.group();
		}
		
		//Pattern record_pattern = Pattern.compile("<tr>\\s+?<td>\\d+</td>\\s+?<td><a href=\"http://fund.eastmoney.com/\\d+.html\" target=\"_blank\">\\d+</a></td>"
		//+"\\s+?<td><a href=\"http://fund.eastmoney.com/\\d+.html\" target=\"_blank\">.+?</a></td>"
		//+"\\s+?<td>[\\.|\\d]+?</td>\\s+?<td><span class=\"red\">[\\.|\\d]+?%</span></td>"
		//+"\\s+?<td>[\\.|\\d]+?</td>\\s+?<td><span class=\"red\">[\\.|\\d]+%</span></td>"    
		//+"\\s+?<td>\\d{4}-\\d{2}-\\d{2}</td>\\s+?<td><a href=\"http://fund.eastmoney.com/f10/jjjl_\\d+.html\" target=\"_blank\">.+?</a></td>\\s+?</tr>");
		Pattern record_pattern = Pattern.compile("<tr>.+?</tr>");
		
		Pattern td_pattern = Pattern.compile("<td>.*?</td>");
		Matcher m3 = record_pattern.matcher(str);
		//for each record
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
			Matcher m4 = td_pattern.matcher(item);
			//column 1
			m4.find();
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
		}
	}
	
	@Override
	public void run() {
		System.out.println(new Date()+":"+url);
		Base.open("com.mysql.jdbc.Driver", FundCrawlerMain.dbConnStr, FundCrawlerMain.dbUserStr, FundCrawlerMain.dbPassStr );
		//curl the page...
		WebDriver driver = new ChromeDriver();

        driver.get( url );
        
        /*
        List<WebElement> records = driver.findElements(By.xpath("//div[@id=\"dataTable\"]/table/tbody/tr"));
        //current date, previous date
        List<WebElement> cols = driver.findElements(By.xpath("//div[@id=\"dataTable\"]/table/tbody/tr[1]/td")); //*[@id="dataTable"]/table/tbody/tr[1]/td[5]
        String curDate = cols.get(3).getText();
        String preDate = cols.get(4).getText();
        //for each record
        String foundCode = null;
        String foundName = null;
        String rate_w1 = null;
        String rate_w2 = null;
        String rate_year1 = null;
        String rate_year2 = null;
        String boot_day = null;
        String manager = null;
        
        int i = 2;
        for( ; i<records.size(); ++i ) {
        	cols = driver.findElements(By.xpath("//div[@id=\"dataTable\"]/table/tbody/tr["+(i+1)+"]/td"));
        	foundCode = cols.get(1).getText();   foundCode.trim();
        	foundName = cols.get(2).getText();   foundName.trim();
        	rate_w1 = cols.get(3).getText();
        	rate_year1 = cols.get(4).getText();   rate_year1.replace('%', ' '); rate_year1.trim();
        	rate_w2 = cols.get(5).getText();
        	rate_year2 = cols.get(6).getText();		rate_year2.replace('%', ' '); rate_year2.trim();
        	boot_day = cols.get(7).getText();
        	manager = cols.get(8).getText();
        	
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
    		int foundID = cf.getInteger("id");

    		this.saveFoundItem(foundID, curDate, Float.parseFloat(rate_w1), Float.parseFloat(rate_year1));
    		this.saveFoundItem(foundID, preDate, Float.parseFloat(rate_w2), Float.parseFloat(rate_year2));
        }
        */
        //Pattern next_reg = Pattern.compile("(?<=<a data-page=\")\\d+?(?=\">下一页</a>)");
        while(true){
        	String src = driver.getPageSource();
        	this.procItems(src);
        	//Matcher m1 = next_reg.matcher(src);
        	//if( m1.find() == false )
        	//	break;
        	//        	
        	By by = By.xpath("//div[@id=\"pageNav\"]/a[text()='下一页']");
        	if( by != null )
        	{
        		List<WebElement> elms = driver.findElements(by);
        		if( elms.size() > 0 ) {
		        	WebElement queryInput=elms.get(0);
		        	if( queryInput != null && queryInput.getAttribute("data-page")!=null) {
		        		Actions action = new Actions(driver);
		           		action.click(queryInput).build().perform();
		           		continue;
		        	}
        		}
        	}
       		break;
        }
        driver.quit();
        /*
        FileWriter fout;
		try {
			fout = new FileWriter( "xcode.html");
			fout.write( src );
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/     
        Base.close();
	}
}
