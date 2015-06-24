package creditcloud.webdrive.model;

import java.util.HashMap;
import java.util.List;

import org.javalite.activejdbc.Model;

public class CreditFund extends Model {
	private static HashMap<String, CreditFund> allFounds = new HashMap<String, CreditFund>();
	
	public static void initFounds(List<CreditFund> founds){
		for( CreditFund item:founds ){
			allFounds.put( item.getString("ori_id"), item );
		}
	}
	public static void setFoundItem( CreditFund item ) {
		allFounds.put( item.getString("ori_id"), item );
	}
	public static CreditFund getFoundByOrgID( String oriID ) {
		return allFounds.get( oriID );
	}
}
