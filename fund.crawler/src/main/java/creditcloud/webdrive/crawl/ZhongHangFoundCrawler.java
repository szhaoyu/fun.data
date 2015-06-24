package creditcloud.webdrive.crawl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javalite.activejdbc.Base;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import creditcloud.webdrive.FundCrawlerMain;
import creditcloud.webdrive.model.CreditFund;

//中航天玑
public class ZhongHangFoundCrawler extends FoundPageCrawlTask {

	public int foundID;
	public String foundOriID;
	private CreditFund foundItem;
	private String scan_time = null;
	private String dtMax = null;
	
	public ZhongHangFoundCrawler(int foundID, String foundOriID, String url) {
		super(url);
		// TODO Auto-generated constructor stub
		this.foundID = foundID;
		this.foundOriID = foundOriID;
	}
	private  boolean procItems(String str ) {
		Pattern date_pattern=Pattern.compile("(?<=<td class=\"gray\" align=\"center\">)\\d{4}-\\d{2}-\\d{2}(?=</td>)");
		Pattern rate1_pattern=Pattern.compile("(?<=<td class=\"gray\" align=\"right\">)[\\d|.]+?(?=</td>)");
		Pattern rate2_pattern=Pattern.compile("(?<=<td class=\"gray\" align=\"right\">)[\\d|.]+?(?=%</td>)");
		
		List<String>  dateList = new ArrayList<String>();
		List<String>  rate1List = new ArrayList<String>();
		List<String>  rate2List = new ArrayList<String>();
		Matcher matcher1 = date_pattern.matcher(str);
		while( matcher1.find() )
		{
			dateList.add(matcher1.group());
		}
		matcher1 = rate1_pattern.matcher(str);
		while( matcher1.find() )
		{
			rate1List.add(matcher1.group());
		}
		matcher1 = rate2_pattern.matcher(str);
		while( matcher1.find() )
		{
			rate2List.add(matcher1.group());
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
				return false;
			if( dtMax == null || dtMax.compareTo(dateList.get(i))<0 )
				dtMax = dateList.get(i);
			//
			saveFoundItem(foundItem, foundID, dateList.get(i), Float.parseFloat(rate1List.get(i)), Float.parseFloat(rate2List.get(i)));
		}
		return true;
	}
	@Override
	public void run() {
		foundItem = CreditFund.getFoundByOrgID( foundOriID );
		if( foundItem == null )
			return;
		//System.out.println(new Date()+":"+url);
		System.out.println(new Date()+":"+foundOriID+":"+url);
		
		Base.open("com.mysql.jdbc.Driver", FundCrawlerMain.dbConnStr, FundCrawlerMain.dbUserStr, FundCrawlerMain.dbPassStr );
		//pre parse....
		dtMax = null;
		scan_time = foundItem.getString("scan_time"); 
		// TODO Auto-generated method stub
		WebDriver driver = new ChromeDriver();
		Pattern item_pattern=Pattern.compile("\\d+");
   
		//中航天玑
        driver.get( url );
        
        while(true)
        {
	        String src=driver.getPageSource();
	        
	        //process current page...
	        if( false == procItems( src ) )
	        	break;
	        
	        Actions action = new Actions(driver);
	        try
        	{
		        By by=By.xpath("//div[@id='page_info']/a");
		        List<WebElement> elements = driver.findElements(by);
		        if( elements.size()>3 )
		        {
		        	String lastPage = elements.get(3).getAttribute("onclick");
		        	String nextPage = elements.get(2).getAttribute("onclick");
		        	int nLast=0, nNext=0;
		            Matcher item_matcher = item_pattern.matcher( lastPage );
		    		while (item_matcher.find()) {
		    			lastPage = item_matcher.group();
		    		}
		    		item_matcher = item_pattern.matcher( nextPage );
		    		while (item_matcher.find()) {
		    			nextPage = item_matcher.group();
		    		}
		    		nLast = Integer.parseInt(lastPage);
		    		nNext = Integer.parseInt(nextPage);
		    		if( nLast < nNext )
		    			break;
		        	
		        	action.click(elements.get(2)).build().perform();
		        }
		        else
		        	break;
        	}
        	catch(Exception e )
        	{
        		e.printStackTrace();
        	}
        }
        driver.quit();
        //
        if( dtMax != null ) {
        	foundItem.setString("scan_time", dtMax);
    		foundItem.save();
        }
        Base.close();
	}
}
