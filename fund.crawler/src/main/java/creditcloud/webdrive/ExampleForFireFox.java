package creditcloud.webdrive;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.xerces.impl.xpath.XPath;
import org.apache.xpath.XPathAPI;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.openqa.selenium.JavascriptExecutor;

import creditcloud.webdrive.crawl.DongFangFoundCrawler;
import creditcloud.webdrive.crawl.FoundPageCrawlTask;
import creditcloud.webdrive.crawl.YuEBaoFoundCrawler;
import creditcloud.webdrive.crawl.ZhongHangFoundCrawler;
import creditcloud.webdrive.model.*;

import org.cyberneko.html.parsers.DOMParser;
import org.dom4j.Document;
import org.dom4j.io.DOMReader;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.javascript.host.NodeList;
import com.google.common.base.Function;


public class ExampleForFireFox {
	public static Properties p = new Properties();
	
	public static void regexTest(String str, String pat ) {
		Pattern item_pattern=Pattern.compile( pat );
        Matcher item_matcher = item_pattern.matcher( str );
        while( item_matcher.find() ) {
        	String grp = item_matcher.group();
        	System.out.println(grp);
        }
        String[] items = item_pattern.split(str);
        System.out.println( items.length );
	}
	public static void insertItem(int feedID, String dStr, float ratePer10k, float ratePerYear )
	{
		CreditRate rate = new CreditRate();
		rate.setInteger("feed_id", feedID );
		//
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		Date dt;
		try {
			dt = sdf.parse(dStr);
			rate.setDate("pubdate", dt);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		GregorianCalendar gc=new GregorianCalendar(); 
		gc.setTime( dt ); 
		gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
		gc.set(GregorianCalendar.MINUTE, 0);
		gc.set(GregorianCalendar.SECOND, 0);
		gc.set(GregorianCalendar.MILLISECOND, 0);
		*/
		rate.setFloat("profit_10k", ratePer10k);
		rate.setFloat("rate_year", ratePerYear);
		rate.save();
	}
	public static void procItems(String str )
	{
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
		for( i=0; i<cnt; ++i )
		{
			insertItem( 121, dateList.get(i), Float.parseFloat(rate1List.get(i)), Float.parseFloat(rate2List.get(i)));
		}
	}
	public static void navigatePages( String url )
	{
		WebDriver driver = new ChromeDriver();
		Pattern item_pattern=Pattern.compile("\\d+");
   
		//中航天玑
        driver.get( url );
        
        while(true)
        {
	        String src=driver.getPageSource();
	        
	        //process current page...
	        procItems( src );
	        
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
	}
	//http://www.thfund.com.cn/website/funds/fundnet.jsp
	public static void navigateYuEBao(String url )
	{
		WebDriver driver = new ChromeDriver();
   
		//中航天玑
        driver.get( url );
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
        
        //selInput
        //for( i=0; options!=null && i<options.size(); ++i )
        {
	        //WebElement selInput=driver.findElement(By.xpath("//div[@id=\"zc2\"]/table[2]/tbody/tr[1]/td/form/table/tbody/tr/td[1]/input"));
        	//String js = "document.getElementsByName(\"fundcode\")[0].value='" + optionStrs.get(i) + "';";
        	String js = "document.getElementsByName(\"fundcode\")[0].value='';";

	        //开始时间 
	        //WebElement startInput=driver.findElement(By.xpath("//input[@id=\"startdate\"]"));
	        js += "document.getElementById(\"startdate\").value='2012-01-01'; ";
	        
	        //结束时间 
	        //WebElement endInput=driver.findElement(By.xpath("//input[@id=\"enddate\"]"));
	        js += "document.getElementById(\"enddate\").value='2020-01-01'; ";
	        
	        ((JavascriptExecutor)driver).executeScript( js );
	        
	        WebElement queryInput=driver.findElement(By.xpath("//div[@id=\"zc2\"]/table[2]/tbody/tr[1]/td/form/table/tbody/tr/td[4]/input"));
	        action.click(queryInput).build().perform();
	        
	        FileWriter fout;
			try {
				fout = new FileWriter( optionNames.get(i)+".html");
				fout.write( driver.getPageSource() );
				fout.close();
		        
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}     
        }
        
        driver.quit();
		
	}
	 public static void print(Node node, String indent) {
	        System.out.println(indent+node.getClass().getName());
	        Node child = node.getFirstChild();
	        while (child != null) {
	            print(child, indent+" ");
	            child = child.getNextSibling();
	        }
	   } // print(Node)
	public static String readFileAsString(String filePath) throws IOException {
	        StringBuffer fileData = new StringBuffer();
	        BufferedReader reader = new BufferedReader(
	                new FileReader(filePath));
	        char[] buf = new char[1024];
	        int numRead=0;
	        while((numRead=reader.read(buf)) != -1){
	            String readData = String.valueOf(buf, 0, numRead);
	            fileData.append(readData);
	        }
	        reader.close();
	        return fileData.toString();
	    }
	 public static void parse(String fileName ) {
		 //InputStream inputStream = new FileInputStream(  fileName );
		 String data = null;
		 try {
			data = readFileAsString( fileName );
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 
		 
		 DOMParser parser = new DOMParser();
		 InputSource inSrc=new InputSource( new ByteArrayInputStream(data.getBytes()) );
		 try {
			parser.parse( inSrc );
			org.w3c.dom.Document document = parser.getDocument();
			//String path="/html/body/div[6]/div[2]/div[4]//dd[@class='wd300 pdl10 fl']/h3/a";
			String path="/HTML/BODY/DIV[6]/DIV[2]/DIV[4]/DL";
			DOMReader domReader = new DOMReader();
			Document doc = domReader.read(document);
			//org.dom4j.Node a=(org.dom4j.Node)doc.selectSingleNode("");
			List<org.dom4j.Node> nodes = (List<org.dom4j.Node>)doc.selectNodes("//DL");
	
			for( org.dom4j.Node item:nodes ){
				System.out.println(item);
			}
/*			
			XPathFactory factory = XPathFactory.newInstance();
			 javax.xml.xpath.XPath xpath = factory.newXPath();
		    XPathExpression expr = xpath.compile(path);
		    		 //"//book[author='Neal Stephenson']/title/text()");
			
			//Node body = parser.getDocument().getLastChild().getLastChild();
			
		    org.w3c.dom.NodeList list = (org.w3c.dom.NodeList)expr.evaluate( parser.getDocument(), XPathConstants.NODESET ); 
					//XPathAPI.selectNodeList( body, path);  
			print(parser.getDocument(), "" );
*/
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	 public static void testXpath(WebDriver driver, String path ) {
		 Actions action = new Actions(driver);
	        //action.click(driver.findElement(By.name("//ul[@class=\'pages\'/li[@class=\'pgNext\'][1]")));
	        //By by=By.name("//ul[contains(@class, 'pages')]/li[contains(@class, 'pgNext')][3]");
	        //By by=By.name("//div[contains(@class, 'pagebott')]");
	        //By by=By.xpath("//div[@id='page_info']/a[3]");
	        //By by=By.xpath("//div[@id=\"pagemini\"]/a[@class=\"next\"]");		//*[@id="loanpage"]/a[13]
	        //div[@id="page1"]/li[7]
	        //By by=By.xpath("//ul[@id=\"page1\"]/li/a[text()='»']");
	        By by=By.xpath( path );
	        if( by != null )
	        {
	        	try
	        	{
			        WebElement element = driver.findElement(by);
			        //List<WebElement> elements = driver.findElements(by);
			        //if( elements.size() >= 3 )
			        if( element != null )
			        {
			        	//WebElement e=elements.get(3);
			        	//String onClick1=e.getAttribute("onclick");
			        	//action.click(elements.get(2)).build().perform();
			        	action.click(element).build().perform();
			        	//JavascriptExecutor js = (JavascriptExecutor) driver;
			        	//js.executeScript(arg0, elements.get(3));
			            //js.executeScript(“arguments[0].click();”,          driver.findElement(By.id(“page_info”)));
			        }
	        	}
	        	catch(Exception e )
	        	{
	        		e.printStackTrace();
	        	}
	        }
	 }
	 public static void testRegEx(String src, String pattern ) {
		 //Pattern item_pattern=Pattern.compile("(?<=href=\")/zhitoubao\\?page=\\d+?(?=\">下一页&gt;</a>)");
	        //href="/zhitoubao?page=2">下一页&gt;</a> 
	        //Pattern item_pattern=Pattern.compile("(?<=<a<a href="?p=2">下一页</a>
		 Pattern item_pattern=Pattern.compile( pattern );
		List<String> item_list = new ArrayList<String>();
		Matcher item_matcher = item_pattern.matcher( src );
		while (item_matcher.find()) {
			item_list.add(StringEscapeUtils.unescapeHtml4(item_matcher.group()));
		}
		for( String item:item_list)
		{
			System.out.println( item );
			System.out.println("http://www.renrendai.com//lend/detailPage.action\\?loanId={}".replaceFirst("\\{\\}", item) );
		}
	 }
	 
	 public static void logFile(String fileName, String data ) {
		 FileWriter fout;
		try {
			fout = new FileWriter( fileName );
			fout.write( data );
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	        
	 }
	 
	 
	public static void main(String[] args) throws IOException {
		//regexTest(content, "(?<=<span>\\d{4}-\\d{2}-\\d{2}</span>)(<span>\\d{4}-\\d{2}-\\d{2}</span>)");
		//regexTest(content, "(<span>\\d{4}-\\d{2}-\\d{2}</span>)(?<=<span>\\d{4}-\\d{2}-\\d{2}</span>)");
	
		//1. 创建一个 FireFox 的浏览器实例
        //WebDriver driver = new FirefoxDriver();
        WebDriver driver = new ChromeDriver();
        //driver.get("http://www.xinxindai.com/borrow/search/list.html");
        //driver.get("http://www.xinxindai.com/borrow/detail/BW201501085955.html");
        //driver.get("http://www.ppmoney.com/xiaodaibao/");
        //driver.get("http://www.xinxindai.com/borrow/search/list.html");
        //driver.get("https://www.jimubox.com/Project/Index/25875");
        
        //driver.get("http://www.eloancn.com/new/loadAllTender.action");
        //driver.get("http://www.eloancn.com/loan/loandetail.action?tenderid=72551&cry=01e5f42a");
        //driver.get("http://www.eloancn.com/new/loadAllWmpsRecords.action");
        
        //String js="tenderQuery(10)";
        //((JavascriptExecutor)driver).executeScript( js );
        
        //driver.get("http://www.fengjr.com/financing/ajax/list?_t=1423194296679&purpose=FENG_RT&maxDuration=200&minInvestAmount=0&minRate=0&maxRate=100&type=cx&status=OPENED&lastLoanId=0&pageSize=300");
        //driver.get("http://www.eloancn.com:80/loan/loandetail.action?tenderid=76825&cry=978fc60f");
        //driver.get("http://www.ppmoney.com/Project/Product/7092");
        //driver.get("https://www.esudai.com/Borrow/Borrow/0fde2eac29a04ad892324bbdc89e2cf3");
        //driver.get("http://www.eloancn.com/new/loadAllTender.action?page=1");
        //driver.get("http://invest.wzdai.com/pagers/invest/detail_450635.html");
        //driver.get("http://www.itouzi.com/dinvest/invest/detail?id=3831774c715143786a6d343d");
        //driver.get("http://www.itouzi.com/dinvest/factoring/detail?id=6e534c75654964464148593d");
        //driver.get("http://www.itouzi.com/dinvest/lease/detail?id=2b307a4555536f49736c593d");
        //driver.get("http://www.itouzi.com/dinvest/list/index?type=5");
        //driver.get("https://www.xinrong.com/invest");
        //driver.get("https://www.xinrong.com/invest/con_detail/13119/23643");
        //driver.get("http://www.renrendai.com/lend/loanList.action");
        //driver.get("http://www.renrendai.com/financeplan/listPlan!listPlan.action");
        //driver.get("http://www.renrendai.com/financeplan/listPlan!listPlanJson.action?category=A&pageIndex=1&_=1424074599410");
        //driver.get("http://www.renrendai.com/financeplan/listPlan!detailPlan.action?financePlanId=120");
        //driver.get("http://www.ppmoney.com/Project/Detail/7329");
        //driver.get("http://www.ppmoney.com/Project/Loan2Detail/7328");
        //driver.get("http://www.srong.com/trade/borrow/success.htm?sys=sr&index=1#pagenav");
        //driver.get("https://www.zhaoshangdai.com/invest/index.html?search=select&status=8&page=4&order=-5&pageNum=10");
        //driver.get("http://www.365edai.cn/lend/sloanlist.aspx?page=1");
        //driver.get("http://www.qdp2p.com/invest/index.html?page=1");
        //driver.get("http://www.js808.cn/newSite/Lend/tb_default.aspx");
        //driver.get("http://www.ksudai.com/Tender/index/p/2");
        //driver.get("https://www.tzydb.com/boot/getInvestIndex/10/1");
        //driver.get("http://www.renrendai.com/lend/detailPage.action?loanId=492360");
        //driver.get("http://www.renrendai.com/lend/loanList!json.action?pageIndex=1");
        driver.get("http://www.jinbaohui.com/proList");

        WebDriverWait wait = new WebDriverWait( driver, 300 );
        /*
        wait.until(new ExpectedCondition<WebElement>(){  
            public WebElement apply(WebDriver d) {  
            	//return d.findElement(By.xpath("/html/body/div[3]/div/div[1]/div[2]/div[4]/a[@class=\"next\"]"));
            	//return d.findElement(By.xpath("//ul[@id=\"loan-list\"]/li[1]"));
            	//return d.findElement(By.xpath("/html/body/div[5]/div/div[2]/table/tbody/tr[2]"));
            	return d.findElement(By.xpath("//table[@id=\"cxls_table\"]/tbody"));
            	//return d.findElement(By.tagName("javascript"));
            	//return d.findElement(By.xpath("//div[@id=\"loan-list-pagination\"]/ul/li/a[@class=\"page-link next\" and contains( text(), \"Next\")]" ));
            			//"/html/body/div[3]/div/div[1]/div[2]/table/tbody/tr[1]"));
            	///html/body/div[3]/div/div[1]/div[2]/div[4]/a[@class="next"]
                //return d.findElement(By.xpath("//ul[@id=\"listContent\"]/li[1]" ));  
            }});  
         */
        String src=driver.getPageSource();
        logFile("debug1.html", src );
        
        //driver.quit();
        
        //driver.get("http://www.sohu.com/");
        
        //<div class="bgfb3 h430 mt20 posiR">.*(?=<div class="financial_list hide clear mt20 bgfb3">)
        //.+<div class=\"financial_list hide clear mt20 bgfb3\">
        //testRegEx(src, "<div class=\"bgfb3 h430 mt20[.|\\s|\\r|\\n]*class=\"financial_list");
        /*
        testRegEx(src, "(?<=<p class=\"ld_status_list0 ml15 pdt5\">)\\s*<span>[^<]*");
        testRegEx(src, "<span>\\d{4}-\\d{2}-\\d{2}</span>");
        testRegEx(src, "(?<=name=\"tenderid\" value=\").+?(?=\")");
        testRegEx(src, "(?<=<h2 class=\"mt10\">).+?(?=</h2>)");
        testRegEx(src, "(?<=colorE6\">)[\\d,]+?(?=</span>元</li>)");
        */
       //ObjectMapper mapper = new ObjectMapper();
       
        ////By by = By.xpath( "//div[contains(@class, \"bgfb3 h430 mt20\")]" );
        //By by = By.xpath("//div[@id='Pagination']/ul/li[@class='pgNext' and contains(text(),'>')]");
        //By by = By.xpath("//div[@id=\"loan-list-pagination\"]/ul/li/a[@class=\"page-link next\" and contains( text(), \"Next\")]" );
        //By by = By.xpath("//table[@id=\"cxls_table\"]/tbody/tr");
        By by = By.xpath("/html/body/div[4]/div/div[1]/ul/li[1]" );
        		//+ "/html/body/div[contains(@class,\"colorbg\")]/div/ul[contains(@class,\"reclist\")]/li");
        WebElement elem2= driver.findElement(by);
        if( elem2 != null ) {
        	String embedTxt = StringEscapeUtils.unescapeHtml4( elem2.getAttribute("innerHTML") ) ;
        	System.out.println( embedTxt );
        }
        
        
        List<WebElement> elems = driver.findElements(by);
        
        WebElement elem1= driver.findElement(by);
        System.out.println(elem1.getAttribute("href"));
        System.out.println(elem1.getAttribute("class"));
        try {
        elem1.findElement(By.xpath("..")).click();
        }
        catch(Exception e ) {
        	e.printStackTrace();
        }
        try {
        driver.findElement(by).click();
        }
        catch(Exception e ) {
        	e.printStackTrace();
        }
        Actions action = new Actions(driver);
		action.click(elem1).build().perform();

        wait.until(new ExpectedCondition<WebElement>(){  
            public WebElement apply(WebDriver d) {  
            	//return d.findElement(By.xpath("/html/body/div[3]/div/div[1]/div[2]/div[4]/a[@class=\"next\"]"));
            	return d.findElement(By.xpath("//ul[@id=\"loan-list\"]/li[1]"));
            	//return d.findElement(By.tagName("javascript"));
            	//return d.findElement(By.xpath("//div[@id=\"loan-list-pagination\"]/ul/li/a[@class=\"page-link next\" and contains( text(), \"Next\")]" ));
            			//"/html/body/div[3]/div/div[1]/div[2]/table/tbody/tr[1]"));
            	///html/body/div[3]/div/div[1]/div[2]/div[4]/a[@class="next"]
                //return d.findElement(By.xpath("//ul[@id=\"listContent\"]/li[1]" ));  
            }});  

        
        List<WebElement> list = driver.findElements(by);
        for( WebElement elem:list ) {
		if( elem != null ) {
			String html = elem.getAttribute("innerHTML");
			System.out.println(html);
		}
		}
			
        /*
        By by = By.xpath( "//ul[@class=\"pages\"]/li[text()='>']" );
		WebElement elem = driver.findElement(by);
		if( elem != null )
		{
			Actions action = new Actions(driver);
			action.click(elem).build().perform();
			//wait for load complete...
			//if( false == (new WebDriverWait()).waitForPageLoad(driver, 300) ) {
				//timeout...
			//}
		}
		*/
        
        //2. test click xpath
        //testXpath( driver, "//ul[@id=\"page1\"]/li/a[text()='»']");
        
        
        //3. after click 获取 网页的 title
        System.out.println("1 Page title is: " + driver.getTitle());
        System.out.println( driver.getCurrentUrl());
             
        src=driver.getPageSource();
        //关闭浏览器
        driver.quit();
        logFile("debug2.html",src);
        
        //4. do regex matching.
        //title: <h4>生意周转 扩大经营<span class="font13 ml20">借款编号：BW201501085955</span><input type="hidden" id="borrowId" value="BW201501085955">
        //testRegEx( src, "(?<=<h4>).*<span class=\"font13 ml20\">.*</span>\\s(?=<input type=\"hidden\" id=\"borrowId\")");
        //testRegEx( src, "(?<=<div class=\"titlebox border-bottom mb20\">)[\\s|\\n]+<h4>[^<>]*");
        
        //amount: <div class="left bid-t1">       <h5>借款金额</h5>       <h1 class="mt5 bold"><span class="red">100,000.00</span><span class="font15">元</span></h1>    </div>
        //testRegEx( src, "<h5>借款金额</h5>[\\s|\\n]*<h1 class=\"mt5 bold\"><span class=\"red\">[\\d|,|\\.]*");
        
        //rate:
        //testRegEx(src, "<h1 class=\"mt5 bold\">[\\n|\\s]*<span class=\"red\">11</span>[\\s|\\n]*<span class=\"font15\">\\%</span>");
        
        //term
        //testRegEx( src, "(?<=<h5>借款期限</h5>)[\\s|\\n]*<h1 class=\"mt5 bold\"><span class=\"red\">6</span><span class=\"font15 grey\">[^<>]*");
        //testRegEx( src, "(?<=href=\")/xiaodaibao\\?page=\\d+?(?=\">下一页&gt;</a>)");
        
        
		//parse("debug.html");
		// Load Properties
		/*
		try {
			InputStream inputStream = new FileInputStream( System.getProperty("user.dir") + File.separator + "config.properties");
			p.load(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		System.out.println("jdbc:mysql://" + p.getProperty("ip") + ":"
				+ p.getProperty("port") + "/" + p.getProperty("db")
				+ "?useUnicode=true&characterEncoding=utf8&autoReconnect=true");

		// OPEN DB Connection
		Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://" + p.getProperty("ip") + ":"
				+ p.getProperty("port") + "/" + p.getProperty("db")
				+ "?useUnicode=true&characterEncoding=utf8&autoReconnect=true",
				p.getProperty("user"), p.getProperty("passwd"));
		//navigatePages("http://www.avictc.com/prod_special_column/historical_net.html");
		//navigateYuEBao("http://www.thfund.com.cn/website/funds/fundnet.jsp");
		LazyList<CreditFund> founds = CreditFund.findAll();
		CreditFund.initFounds(founds);
		FoundPageCrawlTask task1 = new ZhongHangFoundCrawler(  1, "ZH01", "http://www.avictc.com/prod_special_column/historical_net.html" );
		FoundPageCrawlTask task2 = new YuEBaoFoundCrawler(  2, "YEB01", "http://www.thfund.com.cn/website/funds/fundnet.jsp?fundcode=000198&channelid=2&categoryid=2435&childcategoryid=2438&pageno=0" );
		FoundPageCrawlTask task3 = new DongFangFoundCrawler( "http://quote.eastmoney.com/center/fundlist.html#1,0_4" );
		task1.run();
		task2.run();
		task3.run();
		
		Base.close();
		*/
		//return;
		// TODO Auto-generated method stub
		/*
		String mth = "2014-12-25 ";
		if( mth.matches(":"))
		{
			Date nd=new Date();
		}
		GregorianCalendar gc=new GregorianCalendar(); 
		gc.setTime(new Date() ); 
		gc.add( 1, 1);
		System.out.println(gc.getTime());
		gc.add( 5,  1);
		gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
		gc.set(GregorianCalendar.MINUTE, 0);
		gc.set(GregorianCalendar.SECOND, 0);
		gc.set(GregorianCalendar.MILLISECOND, 0);
		System.out.println(gc.getTime());
		*/
		/* for Chrome exporer...
	 	// 设置 chrome 的路径
        System.setProperty(
                "webdriver.chrome.driver",
                "C:\\Documents and Settings\\sq\\Local Settings\\Application Data\\Google\\Chrome\\Application\\chrome.exe");
        // 创建一个 ChromeDriver 的接口，用于连接 Chrome
        @SuppressWarnings("deprecation")
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingChromeDriverExecutable(
                        new File(
                                "E:\\Selenium WebDriver\\chromedriver_win_23.0.1240.0\\chromedriver.exe"))
                .usingAnyFreePort().build();
        service.start();
        // 创建一个 Chrome 的浏览器实例
        WebDriver driver = new RemoteWebDriver(service.getUrl(),
                DesiredCapabilities.chrome());		String str="10000%";
		str.replaceAll("%%", " ");
		Pattern item_pattern1=Pattern.compile("\\d+");
        Matcher item_matcher1 = item_pattern1.matcher( str );
		while (item_matcher1.find()) {
			str=item_matcher1.group();
		}
		 */
		// 如果你的 FireFox 没有安装在默认目录，那么必须在程序中设置
        //System.setProperty("webdriver.firefox.bin", "D:\\Program Files\\Mozilla Firefox\\firefox.exe");


        // 让浏览器访问 Baidu
        //driver.get("http://www.renrendai.com/lend/loanList.action");
        //driver.get("http://www.ppmoney.com/Project/Product/5852");
        //driver.get("http://www.ppmoney.com/anwenying/");
        //driver.get("http://www.ppmoney.com/Project/Product/5852");
        //driver.get("http://www.ppmoney.com/Project/Detail/5879");
        //driver.get("http://www.ppmoney.com/Project/LoanDetail/5722");
        //driver.get("http://www.eloancn.com/new/loadAllTender.action");
        //driver.get("http://www.eloancn.com/loan/loandetail.action?tenderid=59521&cry=26293113");
        //driver.get("http://www.eloancn.com:80/loan/loandetail.action?tenderid=59905&cry=089b9f33%20");
        //中航天玑
        //driver.get("http://www.avictc.com/prod_special_column/historical_net.html");
        //一起好
        //driver.get("https://www.yiqihao.com/loan/list");
        //driver.get("https://www.yiqihao.com/loan/detail/106475");
        //driver.get("http://www.ppmoney.com/anwenying/");
        //driver.get("http://www.ppmoney.com/zhitoubao/");
        
        
        // 用下面代码也可以实现
        // driver.navigate().to("http://www.baidu.com");
 
        //Pattern item_pattern=Pattern.compile("(?<=<a href=\"/view/borrow/)\\d+?(?=\">)");
        //Pattern item_pattern=Pattern.compile("(?<=<a href=\"/view/borrow/)\\d+?(?=\">)");
        //Pattern item_pattern=Pattern.compile("(?<= href=)\d+?(?=\“>)";
        //Pattern item_pattern=Pattern.compile("(?<=target=\"_blank\" href=\")/lend/detailPage.action?loanId=\\d+?(?=\")");
        //Pattern item_pattern=Pattern.compile("href=\"/lend/detailPage.action?loanId=");
        //Pattern item_pattern=Pattern.compile("(?<=href=\")/lend/detailPage.+?(?=\")");
        //Pattern item_pattern=Pattern.compile("(?<=<a class=\"page-link next\" href=\")#page-\\d+?(?=\">)");
        //Pattern item_pattern=Pattern.compile("<em id=\"span_CirculationMoney\">.+?</em>元");
        //Pattern item_pattern=Pattern.compile("(?<=<span>融资余额:</span>)\\s<em id=\"span_CirculationMoney\">[\\d|,|.]+</em>元\\s?(?=</div>)");
        //>扩大生产/经营</a><a class="page-link next" href="
        //Pattern item_pattern=Pattern.compile("(?<=href=\")/Project/Product/\\d+?(?=\" target=\"_blank\">)");
        //Pattern item_pattern=Pattern.compile("(?<=<a target=\"_blank\" href=\")/Project/Product/\\d+?(?=\" title=\")");
        //Pattern item_pattern=Pattern.compile("(?<=<a class=\"ml5 fc_dc0\" href=\")/anwenying\\?page=\\d+?(?=\">下一页&gt;</a>)"); 
        //Pattern item_pattern=Pattern.compile("(?<=<a href=\")/anwenying\\?page=\\d+?(?=\"[^<>]+?>下一页)");
        //PPMoney 标题正则式
        //Pattern item_pattern=Pattern.compile("(?<=<h1>).+?(?=</h1>\\s+?<div class=\"l-proj-c\">)");
        //Pattern item_pattern=Pattern.compile("(?<=<span>投资期限:</span>).+?(?=</li>)");
        //<span>投资期限:</span> <span class="value">30<span>天</span></span>
        //Pattern item_pattern=Pattern.compile("(?<=<span>投资期限:</span>)[^<]+?<span\\s+class=\"value\">\\d+?<span>天(?=</span></span>)");
        //<span>偿还方式：</span><span>到期一次性返本付息</span>
        //Pattern item_pattern=Pattern.compile("(?<=<span>偿还方式：</span><span>).+?(?=</span>)");
        //申购时间       <li>        <span>申购时间：</span>\\s+?<span>[\\d| |-|:]+?&nbsp;&nbsp;至&nbsp;&nbsp;[\\d| |-|:]+?</span></li>
        //<li>        <span>申购时间：</span>[\t| |\r|\n]+?        <span>2014-12-24 11:00  至  2014-12-29 11:00</span>    </li>

        //Pattern item_pattern=Pattern.compile("(?<=<span>申购时间：</span>)[\t| |\r|\n]+?<span>\\d{4}-\\d{2}-\\d{2}[\\d| |:]+?(?=至)");
        //Pattern item_pattern=Pattern.compile("<span>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}(?=  至)");
        //Pattern item_pattern=Pattern.compile("(?<=至  )\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}(?=</span)");
        //Pattern item_pattern=Pattern.compile("(?<=<span>投资期限:</span>)\\s+<span class=\"fcc20\">\\d+</span>[个|月|天|年|  |\\s]+?(?=</li>)");
        //Pattern item_pattern=Pattern.compile("(?<=<span>融资期限：</span><span class=\"value\">)\\d+</span><span>[个|月|天|年| |\\s]+?(?=</span>)"); 
        //Pattern item_pattern=Pattern.compile("(?<=<span>申购时间：</span>\\s+?<span>)[\\d| |-|:]+?&nbsp;&nbsp;(至&nbsp;&nbsp;[\\d| |-|:]+?</span>");
        //Pattern item_pattern=Pattern.compile("<a target=\"_blank\" href=\"/Project/Detail/5879\" title=");
        //Pattern item_pattern=Pattern.compile("(?<=<li class=\"page-number pgCurrent\">\\d+</li>)<li class=\"page-number\">");
        //Pattern item_pattern=Pattern.compile("(?<=value=\")\\d+(?=\" name=\"tenderid\")");
        //<span id="interestrate_59106_2_443822" class="font22 colorE6">18</span>
        //interestrate_59106_2_443822
        ///利率：<span class="font22 colorE6" id="interestrate_59106_2_443822">18</span>%/年		</li>
        //Pattern item_pattern=Pattern.compile("(?<=<li class=\"wd300\">利率：)<span class=\"font22 colorE6\" id=\"interestrate_[\\d|_]+?\">[\\d|.]+</span>.+?(?=</li>)");
        //Pattern item_pattern=Pattern.compile("(?<=<li class=\"wd300\">利率：)<span class=\"font22 colorE6\" id=\".+</span>");
        //Pattern item_pattern=Pattern.compile("(?<=<p class=\"ld_status_list0 ml15 pdt5\">)\\s+<span>[\\d|-]+(?=</span>)");
        //Pattern item_pattern=Pattern.compile("(?<=<span>)\\d{4}-\\d{2}-\\d{2}</span>\\s+</p>\\s+(?=<!-- 待终审)");
        //Pattern item_pattern=Pattern.compile("(?<=<li class=\"wd180\">期限：<span class=\"font22 colorE6\">).+(?=</li>)");
        //Pattern item_pattern=Pattern.compile("(?<=<p class=\"ld_status_list0 ml15 pdt5\">)\\s+<span>[\\d|-]+(?=</span>)");
        //Pattern item_pattern=Pattern.compile("\\d{4}-\\d{2}-\\d{2}</span>\\s+</p>\\s+(?=<!-- 待终审)");
        //Pattern item_pattern=Pattern.compile("(?<=借款进度： </span>)\\s*<span class=\"plan\">\\s*<em style=\"width:100.0%;background-color:#7cae4a;\">\\d+%</em>");
        //Pattern item_pattern=Pattern.compile("(?<=<p class=\"ld_status_list0 ml15 pdt5\">)\\s*<span>[\\d|-]+(?=</span>)");
        //Pattern item_pattern=Pattern.compile("<span>\\d{4}-\\d{2}-\\d{2}</span>");
        
        //<td class="gray" align="center">2014-12-04</td>
        //<td class="gray" align="right">1.5906</td>
        //<td class="gray" align="right">6.0804%</td>
        //Pattern item_pattern=Pattern.compile("(?<=<td class=\"gray\" align=\"center\">)\\d{4}-\\d{2}-\\d{2}</td>");
        //<a href="http://www.eloancn.com:80/loan/loandetail.action?tenderid=61236&amp;cry=e629f755 " target="_blank" class="fl">【生意周转】 馆陶用于经...</a>
        //Pattern item_pattern=Pattern.compile("(?<=<a href=\")http://www.eloancn.com:80/loan/.+?(?=\" target=\"_blank\" class=\"fl\")");
        //Pattern item_pattern=Pattern.compile("<li class=\"page-number pgCurrent\">\\d+</li><li class=\"page-number\">\\d+</li>");
        //Pattern item_pattern=Pattern.compile("(?<=<a href=\"/loan/detail/)\\d+(?=\" class=\"darkblue\" title=\")");
        //Pattern item_pattern=Pattern.compile("(?<=<span class=\"darkred\">)[\\d|\\.]+\\%");
        //Pattern item_pattern=Pattern.compile("(?<=流转总额<br /><span class=\"darkred\">[￥]?)[\\d|,|\\.]*(?=</span></div>)");
        //Pattern item_pattern=Pattern.compile("(?<=<span class=\"progress\" style=\"width:)\\d+%(?=;\"></span>)");
        //Pattern item_pattern=Pattern.compile("(?<=<a target=\"_blank\" href=\")/Project/Product/\\d+(?=\" title=\")");
        //Pattern item_pattern=Pattern.compile("(?<=href=\")/anwenying\\?page=\\d+(?=\">下一页&gt;</a>)");
        
        
        // 通过 id 找到 input 的 DOM
       // WebElement element = driver.findElement(By.id("kw"));
 
        // 输入关键字
        //element.sendKeys("zTree");
 
        // 提交 input 所在的  form
        //element.submit();
         
        // 通过判断 title 内容等待搜索页面加载完毕，间隔10秒
        //(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
         //   public Boolean apply(WebDriver d) {
           //     return d.getTitle().toLowerCase().endsWith("ztree");
           // }
        //});
 
        // 显示搜索结果页面的 title
        //System.out.println("2 Page title is: " + driver.getTitle());
	}

}
