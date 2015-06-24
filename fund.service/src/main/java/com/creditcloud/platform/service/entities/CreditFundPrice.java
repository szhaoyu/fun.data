package com.creditcloud.platform.service.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@Entity
@Table(name = "credit_fund_prices")
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class CreditFundPrice implements Serializable {
	private static final long serialVersionUID = 1251823223158908981L;
	
	@Id
    @Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	/*
	@Column(name = "credit_fund_id")
	private Integer credit_fund_id;
	*/
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "credit_fund_id", referencedColumnName = "id")
    private CreditFund fund;
	
	@Column(name = "pubdate", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Calendar pubdate;
	
	@Column(name = "profit_10k")
    private Float profit_10k;
	
	@Column(name = "rate_year")
    private Float rate_year;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public CreditFund getFund() {
		return fund;
	}

	public void setFund(CreditFund fund) {
		this.fund = fund;
	}

	public Calendar getPubdate() {
		return pubdate;
	}

	public void setPubdate(Calendar pubdate) {
		this.pubdate = pubdate;
	}

	public Float getProfit_10k() {
		return profit_10k;
	}

	public void setProfit_10k(Float profit_10k) {
		this.profit_10k = profit_10k;
	}

	public Float getRate_year() {
		return rate_year;
	}

	public void setRate_year(Float rate_year) {
		this.rate_year = rate_year;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
