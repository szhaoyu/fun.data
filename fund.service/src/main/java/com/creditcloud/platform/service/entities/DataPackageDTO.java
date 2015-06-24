package com.creditcloud.platform.service.entities;

import java.util.ArrayList;

public class DataPackageDTO {
	private	String  	type;
	private Integer 	totals;
	private ArrayList	data;
	
	public DataPackageDTO(String type, ArrayList list ) {
		this.type = type;
		this.data = list;
		this.totals = list.size();
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getTotals() {
		return totals;
	}
	public void setTotals(Integer totals) {
		this.totals = totals;
	}
	public ArrayList getData() {
		return data;
	}
	public void setData(ArrayList data) {
		this.data = data;
	}
	
}
