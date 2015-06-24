package creditcloud.webdrive.crawl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javalite.activejdbc.Base;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import creditcloud.webdrive.FundCrawlerMain;
import creditcloud.webdrive.model.CreditFund;

public class YuEBaoFoundCrawler extends FoundPageCrawlTask {

	public int foundID;
	public String foundOriID;
	private CreditFund foundItem;
	private String scan_time = null;
	private String dtMax = null;
	
	public YuEBaoFoundCrawler(int foundID, String foundOriID, String url) {
		super(url);
		// TODO Auto-generated constructor stub
		this.foundID = foundID;
		this.foundOriID = foundOriID;
	}
	
	public  void procItems(String str ) {
		/*
		<tr>
        <td height="34" align="center" valign="middle">2014-12-28</td>
        <td height="34" align="center" valign="middle">1.2609</td>
        <td height="34" align="center" valign="middle">4.8520</td>
       
      </tr>
      */
		Pattern date_pattern=Pattern.compile("<tr>\\s+?<td height=\"34\" align=\"center\" valign=\"middle\">\\d{4}-\\d{2}-\\d{2}(?=</td>)");
		Pattern rate1_pattern=Pattern.compile("</td>\\s+?<td height=\"34\" align=\"center\" valign=\"middle\">[\\d|.]+?(?=</td>)");
		Pattern rate2_pattern=Pattern.compile("(?<=<td height=\"34\" align=\"center\" valign=\"middle\">)[\\d|.]+?</td>\\s+?</tr>");
		
		List<String>  dateList = new ArrayList<String>();
		List<String>  rate1List = new ArrayList<String>();
		List<String>  rate2List = new ArrayList<String>();
		Matcher matcher1 = date_pattern.matcher(str);
		while( matcher1.find() )
		{
			String dt = matcher1.group().replaceAll("<[^>]+?>", "");
			dateList.add(dt.trim());
		}
		matcher1 = rate1_pattern.matcher(str);
		while( matcher1.find() )
		{
			String dt = matcher1.group().replaceAll("<[^>]+>", "");
			rate1List.add(dt.trim());
		}
		matcher1 = rate2_pattern.matcher(str);
		while( matcher1.find() )
		{
			String dt = matcher1.group().replaceAll("<[^>]+>", "");
			rate2List.add(dt.trim());
		}
		int i, cnt;
		cnt = dateList.size();
		/*
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		Date dt, dtMax=null;
		Date scan_time = foundItem.getDate("scan_time");
		*/
		for( i=0; i<cnt; ++i )
		{
			if( scan_time != null && scan_time.compareTo(dateList.get(i))>=0 )
				break;
			if( dtMax == null || dtMax.compareTo(dateList.get(i))<0 )
				dtMax = dateList.get(i);
			
			//
			saveFoundItem(foundItem, foundID, dateList.get(i), Float.parseFloat(rate1List.get(i)), Float.parseFloat(rate2List.get(i)));
		}
	}
	
	@Override
	public void run() {
		
		foundItem = CreditFund.getFoundByOrgID( foundOriID );
		if( foundItem == null )
			return;
		System.out.println(new Date()+":"+foundOriID+":"+url);
		Base.open("com.mysql.jdbc.Driver", FundCrawlerMain.dbConnStr, FundCrawlerMain.dbUserStr, FundCrawlerMain.dbPassStr );
		//pre parse....
		dtMax = null;
		scan_time = foundItem.getString("scan_time"); 
		// TODO Auto-generated method stub
		WebDriver driver = new ChromeDriver();
		   
		//余额宝
        driver.get( url );
        String src=null;
        /*
        //selector
        List<WebElement> options=driver.findElements(By.xpath("//div[@id=\"zc2\"]/table[2]/tbody/tr[1]/td/form/table/tbody/tr/td[1]/select/option"));
        List<String>   optionStrs = new ArrayList<String>();
        List<String>   optionNames = new ArrayList<String>();
        int i=0; 
        for( ; options!=null && i<options.size(); ++i )
        {
        	optionStrs.add(options.get(i).getAttribute("value"));
        	optionNames.add(options.get(i).getText());
        }
        
        Actions action = new Actions(driver);
        String src = null;
        //selInput
        //for( i=0; options!=null && i<options.size(); ++i )
        {
	        //WebElement selInput=driver.findElement(By.xpath("//div[@id=\"zc2\"]/table[2]/tbody/tr[1]/td/form/table/tbody/tr/td[1]/input"));
        	//String js = "document.getElementsByName(\"fundcode\")[0].value='" + optionStrs.get(i) + "';";
        	String js = "document.getElementsByName(\"fundcode\")[0].value='000198';";

	        //开始时间 
	        //WebElement startInput=driver.findElement(By.xpath("//input[@id=\"startdate\"]"));
        	if( scan_time == null )
        		js += "document.getElementById(\"startdate\").value='2012-01-01'; ";
        	else
        		js += "document.getElementById(\"startdate\").value='"+scan_time+"'; ";
	        
	        //结束时间 
	        //WebElement endInput=driver.findElement(By.xpath("//input[@id=\"enddate\"]"));
	        js += "document.getElementById(\"enddate\").value='2020-01-01'; ";
	        
	        ((JavascriptExecutor)driver).executeScript( js );
	        
	        WebElement queryInput=driver.findElement(By.xpath("//div[@id=\"zc2\"]/table[2]/tbody/tr[1]/td/form/table/tbody/tr/td[4]/input"));
	        action.click(queryInput).build().perform();
	        
	        src = driver.getPageSource();
	        
	        FileWriter fout;
			try {
				//fout = new FileWriter( optionNames.get(i)+".html");
				fout = new FileWriter( "xcode.html");
				fout.write( src );
				fout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}     
        }
        */
        src = driver.getPageSource();
        driver.quit();
        if( src != null )
        	this.procItems(src);
        //
        if( dtMax != null ) {
        	foundItem.setString("scan_time", dtMax);
    		foundItem.save();
        }
        Base.close();
	}
	
}
