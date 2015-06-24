package com.creditcloud.platform.service.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@Entity
@Table(name = "credit_funds")
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class CreditFund implements Serializable {
	private static final long serialVersionUID = 1250823223158908981L;

	@Id
    @Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@Column(name = "ori_id", length = 32,  updatable = true)
    @Size(max = 32)
    private String ori_id;
	
	@Column(name = "name", length = 64,  updatable = true)
    @Size(max = 64)
    private String name;

	@Column(name = "boot_day", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Calendar boot_day;
	
	@Column(name = "manager", length = 128,  updatable = true)
    @Size(max = 128)
    private String manager;
	
	@Column(name = "feed_id")
    private Integer feed_id;
	
	@Column(name = "scan_time", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Calendar scan_time;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "fund")
    @OrderBy("pubdate desc")
    @JsonIgnore
    private final List<CreditFundPrice> prices = new ArrayList<>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOri_id() {
		return ori_id;
	}

	public void setOri_id(String ori_id) {
		this.ori_id = ori_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Calendar getBoot_day() {
		return boot_day;
	}

	public void setBoot_day(Calendar boot_day) {
		this.boot_day = boot_day;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public Integer getFeed_id() {
		return feed_id;
	}

	public void setFeed_id(Integer feed_id) {
		this.feed_id = feed_id;
	}

	public Calendar getScan_time() {
		return scan_time;
	}

	public void setScan_time(Calendar scan_time) {
		this.scan_time = scan_time;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<CreditFundPrice> getPrices() {
		return prices;
	}
	
}
