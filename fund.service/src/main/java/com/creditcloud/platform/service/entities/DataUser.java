package com.creditcloud.platform.service.entities;

import java.io.Serializable;
import java.util.ArrayList;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@Entity
@Table(name = "t_data_user")
@NamedQueries({
    @NamedQuery(
	    name = "DataUser.findByUrl",
	    query
	    = "Select u from DataUser u where u.receive_url = :url"
   ),
   @NamedQuery(
		    name = "DataUser.findByTicket",
		    query
		    = "Select u from DataUser u where u.ticket = :ticket"
	   )
})
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class DataUser implements Serializable {
	private static final long serialVersionUID = 1249823223158908981L;
	
	@Id
    @Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@Column(name = "name", length = 64,  updatable = true)
    @Size(max = 64)
    private String name;
	
	@Column(name = "home", length = 64,  updatable = true)
    @Size(max = 64)
    private String home;
	
	@Column(name = "data_type", length = 32,  updatable = true)
    @Size(max = 32)
    private String data_type;
	
	@Column(name = "receive_url", length = 128,  updatable = true)
    @Size(max = 128)
    private String receive_url;
	
	//3FC0F17E-901B-9009-C7F0-F72D00C576A7
	@Column(name = "ticket", length = 40,  updatable = true)
    @Size(max = 40)
    private String ticket;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    @OrderBy("op_date desc")
    @JsonIgnore
    private final List<ServiceLog> logs = new ArrayList<ServiceLog>();
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHome() {
		return home;
	}

	public void setHome(String home) {
		this.home = home;
	}

	public String getData_type() {
		return data_type;
	}

	public void setData_type(String data_type) {
		this.data_type = data_type;
	}

	public String getReceive_url() {
		return receive_url;
	}

	public void setReceive_url(String receive_url) {
		this.receive_url = receive_url;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public List<ServiceLog> getLogs() {
		return logs;
	}


}
