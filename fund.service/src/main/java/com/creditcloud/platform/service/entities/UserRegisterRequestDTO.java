package com.creditcloud.platform.service.entities;

import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegisterRequestDTO {
	@NotBlank
    @Size(max = 64)
	private String name;
	
	@NotBlank
    @Size(max = 64)
	private String home;
	
	@NotBlank
    @Size(max = 32)
	private String dataType;
	
	@NotBlank
    @Size(max = 128)
	private String receiveUrl;
	
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
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getReceiveUrl() {
		return receiveUrl;
	}
	public void setReceiveUrl(String receiveUrl) {
		this.receiveUrl = receiveUrl;
	}
	
}
