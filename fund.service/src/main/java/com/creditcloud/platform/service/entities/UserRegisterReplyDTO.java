package com.creditcloud.platform.service.entities;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegisterReplyDTO {
	@NotBlank
	private int 	status;	// -1,申请失败； 0，成功； 1，重复申请
	
	@NotBlank
    @Size(max = 40)
	private String 	ticket; //为申请用户生成的ticket
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

}
