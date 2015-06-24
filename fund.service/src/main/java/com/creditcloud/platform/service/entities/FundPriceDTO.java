package com.creditcloud.platform.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundPriceDTO {
	private String name;
	private String id;
	private String pubdate;
	private Float profit_10k;
	private Float rate_7d;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPubdate() {
		return pubdate;
	}
	public void setPubdate(String pubdate) {
		this.pubdate = pubdate;
	}
	public Float getProfit_10k() {
		return profit_10k;
	}
	public void setProfit_10k(Float profit_10k) {
		this.profit_10k = profit_10k;
	}
	public Float getRate_7d() {
		return rate_7d;
	}
	public void setRate_7d(Float rate_7d) {
		this.rate_7d = rate_7d;
	}
}
