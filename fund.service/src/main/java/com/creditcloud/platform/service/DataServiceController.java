package com.creditcloud.platform.service;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.creditcloud.platform.service.entities.DataPackageDTO;
import com.creditcloud.platform.service.entities.DataRequestDTO;
import com.creditcloud.platform.service.entities.DataUser;
import com.creditcloud.platform.service.entities.FundPriceDTO;
import com.creditcloud.platform.service.entities.ServiceLog;
import com.creditcloud.platform.service.repositories.DataUserRepository;
import com.creditcloud.platform.service.repositories.ServiceLogRepository;

@RestController
public class DataServiceController {
	@PersistenceContext
	EntityManager em;
	
	private DataUserRepository userRepo;
	private ServiceLogRepository logRepo;
	
	@Autowired
	public DataServiceController(DataUserRepository userRepo, ServiceLogRepository logRepo ) {
		this.userRepo = userRepo;
		this.logRepo = logRepo;
	}
	
	//1. API:  2015-01-28, GET simple query
	//@RequestMapping(value = "/api/dataService/funds/{ticket}", method = GET )
	@RequestMapping(value = "/funds/{ticket}", method = GET )
	public DataPackageDTO getFunds( HttpServletRequest request, 
			final @PathVariable("ticket") String ticket ) {
		DataUser user = this.userRepo.findByTicket(ticket);
		if(user == null ) {
			return new DataPackageDTO("funds", new ArrayList<FundPriceDTO>());
		}
		ArrayList<FundPriceDTO> list = queryFundsData( "order by b.pubdate desc limit 50" );
		//got ip address
		String host = getIpAddr(request); 
		//save log
		this.logRepo.save( new ServiceLog( user,"GET", "/funds/{ticket}", "", host ) );
		
		return new DataPackageDTO("funds", list);
	}
	
	//2. API: 2015-01-28, POST advanced query
	//@RequestMapping(value = "/api/dataService/funds/{ticket}", method = POST)
	@RequestMapping(value = "/funds/{ticket}", method = POST)
	public DataPackageDTO getFundsWithCondition( HttpServletRequest request, final @PathVariable("ticket") String ticket, final @RequestBody @Valid  DataRequestDTO cmd, final BindingResult bindingResult ) {	
		if(bindingResult.hasErrors()) {
		    throw new IllegalArgumentException("Invalid arguments.");
		}
		
		//ServiceLog log = new ServiceLog();
		DataUser user = this.userRepo.findByTicket(ticket);
		if( user == null )
			return new DataPackageDTO("funds",new ArrayList<FundPriceDTO>());

		//got ip address
		String host = getIpAddr(request); 
				
		StringBuilder build = this.buildConditionSQL( new StringBuilder(), cmd.getIds(), cmd.getPubdate(), cmd.getMindate(), cmd.getMaxdate());
		build = this.buildOrderBySQL(build, null);
		
		DataPackageDTO data = new DataPackageDTO("funds", queryFundsData( build.toString() ) );
		
		//save log
		this.logRepo.save( new ServiceLog( user,"POST", "/funds/{ticket}", cmd.toString(), host ) );
		
		return data;
	}

	//2(1). API: 2015-01-28, POST advanced query. support URL parameter include 'ticket'
	//@RequestMapping(value = "/api/dataService/funds", method = POST)
	@RequestMapping(value = "/funds", method = POST)
	public DataPackageDTO getFundsWithConditionParam( HttpServletRequest request,
			final @RequestParam(value="ticket",required=true) String ticket,	//final @PathVariable("ticket") String ticket, 
			final @RequestParam(value="all",required=false,defaultValue="true") Boolean all,
			final @RequestParam(value="count",required=false) boolean count,
			final @RequestBody @Valid  DataRequestDTO cmd, final BindingResult bindingResult ) {	
		if(bindingResult.hasErrors()) {
		    throw new IllegalArgumentException("Invalid arguments.");
		}
		
		//ServiceLog log = new ServiceLog();
		DataUser user = this.userRepo.findByTicket(ticket);
		if( user == null )
			return new DataPackageDTO("funds",new ArrayList<FundPriceDTO>());
		//
		DataPackageDTO result = this.getFundsInternal(count, all, cmd.getIds(), cmd.getPubdate(), cmd.getMindate(), cmd.getMaxdate());
		//got ip address
		String host = getIpAddr(request); 
		//save log
		this.logRepo.save( new ServiceLog( user,"POST", "/funds", cmd.toString(), host ) );
		
		return result;
	}

	//3. API: 2015-01-29, GET advanced query
	@RequestMapping(value = "/funds", method = GET )
	public DataPackageDTO getFunds( HttpServletRequest request,
			final @RequestParam(value="ticket",required=true) String ticket,
			final @RequestParam(value="all",required=false) boolean all,
			final @RequestParam(value="count",required=false) boolean count,
			final @RequestParam(value="id",required=false) String id, 
			final @RequestParam(value="pubdate",required=false) String pubdate,
			final @RequestParam(value="mindate",required=false) String mindate,
			final @RequestParam(value="maxdate",required=false) String maxdate
			) {
		////
		DataUser user = null;
		if(ticket == null || (user = this.userRepo.findByTicket(ticket)) == null ) {
			return new DataPackageDTO("funds", new ArrayList<FundPriceDTO>());
		}
		////		
		DataPackageDTO result = this.getFundsInternal(count, all, id, pubdate, mindate, maxdate);

		//got ip address
		String host = getIpAddr(request);
		//save log
		StringBuilder paramBuild = new StringBuilder();
		paramBuild.append("count=");			paramBuild.append(count);
		paramBuild.append("&all=");				paramBuild.append(all);
		paramBuild.append("&id=");				paramBuild.append(id);
		paramBuild.append("&pubdate=");			paramBuild.append(pubdate);
		paramBuild.append("&mindate=");			paramBuild.append(mindate);
		paramBuild.append("&maxdate=");			paramBuild.append(maxdate);
		this.logRepo.save( new ServiceLog( user, "GET", "/funds", paramBuild.toString(), host ) );

		return result;
	}
	
	//internal use functions
	private DataPackageDTO getFundsInternal(boolean count, boolean all, String id, String pubdate, String mindate, String maxdate ) {
		StringBuilder build = this.buildConditionSQL( new StringBuilder(), id, pubdate, mindate, maxdate);
		
		DataPackageDTO result = null;
		if( count ) {
			result = new DataPackageDTO("funds", new ArrayList<FundPriceDTO>());
			Object cnt = queryFundsCount(build.toString());
			result.setTotals((Integer)cnt);
		}
		else {
			this.buildOrderBySQL(build, null);
			if( all == false )
				build = this.buildOffsetSQL(build, 0, 50);

			result = new DataPackageDTO("funds", queryFundsData( build.toString() ));
		}
		
		return result;
	}
	
	private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknow".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }
	
	//navtive query mysql
	private int queryFundsCount(String condition ) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("select count(*) from credit_funds a, credit_fund_prices b where a.id=b.credit_fund_Id ");
		if(condition!=null && condition.isEmpty() == false ) {
			builder.append( condition );
		}
		
		Query query = em.createNativeQuery( builder.toString());
		//Object obj = query.getSingleResult();
		BigInteger count =(BigInteger) query.getSingleResult();
		if(count!=null)
			return count.intValue();
		return 0;
	}
	private ArrayList<FundPriceDTO> queryFundsData(String condition ) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("select a.name as name,a.ori_id as id,b.pubdate as pubdate,b.profit_10k as profit_10k,b.rate_year as rate_7d from credit_funds a, credit_fund_prices b where a.id=b.credit_fund_Id ");
		if(condition!=null && condition.isEmpty() == false ) {
			builder.append( condition );
		}
		
		Query query = em.createNativeQuery( builder.toString());//, FundPriceDTO.class );
		List list = query.getResultList();
		ArrayList<FundPriceDTO>	listPrice = new ArrayList<FundPriceDTO>();
		if( list != null ) {
			for(Object obj:list ) {
				Object[] fields = (Object[])obj;
				FundPriceDTO price = new FundPriceDTO();
				
				price.setName( (String) fields[0] );
				price.setId( (String) fields[1]);
				Timestamp pdate = (Timestamp) fields[2];
				price.setPubdate( pdate.toString());
				price.setProfit_10k( (Float) fields[3]);
				price.setRate_7d((Float)fields[4]);
					
				listPrice.add(price);
			}
		}
		
		return listPrice;
	}
	
	//build where condition SQL
	private StringBuilder buildConditionSQL( StringBuilder build, String ids, String pubdate, String mindate, String maxdate ) {
		//ids: filter
		if( ids!=null && !ids.isEmpty() ) {
			String[]idArr = ids.split(",");
			if( idArr.length > 0 ) {
				build.append("and a.ori_id in('");
				build.append(idArr[0]);
				for(int i=1; i<idArr.length; ++i) {
					build.append("','");
					build.append(idArr[i]);
				}
				build.append("') ");
			}
		}
		//pubdate:
		if(pubdate != null && !pubdate.isEmpty()) {
			build.append("and b.pubdate='");
			build.append(pubdate);
			build.append("' ");
		}
		//mindate:
		if(mindate != null && !mindate.isEmpty()) {
			build.append("and b.pubdate>='");
			build.append(mindate);
			build.append("' ");
		}
		//maxdate
		if(maxdate != null && !maxdate.isEmpty()) {
			build.append("and b.pubdate<='");
			build.append(maxdate);
			build.append("' ");
		}
		
		
		
		return build;
	}
	
	private StringBuilder buildOrderBySQL( StringBuilder build, String orderBy ) {
		//append order by
		build.append("order by b.pubdate desc ");
		if( orderBy != null && !orderBy.isEmpty() ) {
			build.append(",");
			build.append(orderBy);
			build.append(" ");
		}
		return build;
	}
	
	//build offset limit SQL
	private StringBuilder buildOffsetSQL(StringBuilder build, int offset, int count ) {
		if( count > 0 ) {
			build.append("limit ");
			build.append(offset);
			build.append(",");
			build.append(count);
		}
		return build;
	}
}
