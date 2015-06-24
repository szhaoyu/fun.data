package com.creditcloud.platform.service.entities;

import java.io.Serializable;
import java.util.Calendar;
import java.util.TimeZone;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@Entity
@Table(name = "t_data_service_log")
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class ServiceLog implements Serializable {
	private static final long serialVersionUID = 1249824815158908981L;

	@Id
    @Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	/*
	@Column(name = "user_id", nullable = false, updatable = true)
    private Integer user_id;
	*/
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private DataUser user;
	
	@Column(name = "type", length = 16,  updatable = true)
    @Size(max = 16)
    private String type;		//POST. GET
	
	@Column(name = "op_date")
    @Temporal(TemporalType.DATE)
    private Calendar op_date;
	
	@Column(name = "url", length = 128,  updatable = true)
    @Size(max = 128)
    private String url;
	
	//3FC0F17E-901B-9009-C7F0-F72D00C576A7
	@Column(name = "params", length = 256,  updatable = true)
    @Size(max = 256)
    private String params;

	@Column(name = "host", length = 32,  updatable = true)
    @Size(max = 32)
    private String host;
	
	public ServiceLog( DataUser user, String type, String url, String params, String host ) {
		this.user = user;
		this.type = type;
		this.url = url;
		this.params = params;
		this.host = host;
		this.op_date = Calendar.getInstance();
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Calendar getOp_date() {
		return op_date;
	}

	public void setOp_date(Calendar op_date) {
		this.op_date = op_date;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public DataUser getUser() {
		return user;
	}

	public void setUser(DataUser user) {
		this.user = user;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}


}
