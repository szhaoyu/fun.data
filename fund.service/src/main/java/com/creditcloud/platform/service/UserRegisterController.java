package com.creditcloud.platform.service;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creditcloud.platform.service.entities.DataUser;
import com.creditcloud.platform.service.entities.UserRegisterReplyDTO;
import com.creditcloud.platform.service.entities.UserRegisterRequestDTO;
import com.creditcloud.platform.service.repositories.DataUserRepository;

@RestController
public class UserRegisterController {
	private final DataUserRepository userRepo;
	
	@Autowired
	public UserRegisterController( DataUserRepository userRepo ) {
		this.userRepo = userRepo;
	}
	
	@RequestMapping(value = "/api/userRegister", method = POST)
	//@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserRegisterReplyDTO> doRegister( final @RequestBody @Valid  UserRegisterRequestDTO cmd, final BindingResult bindingResult) {	
		if(bindingResult.hasErrors()) {
		    throw new IllegalArgumentException("Invalid arguments.");
		}
		UserRegisterReplyDTO reply = new UserRegisterReplyDTO();
		ResponseEntity<UserRegisterReplyDTO> rv;
		
		DataUser user = this.userRepo.findByUrl( cmd.getReceiveUrl() );
		if( user != null ) {
			reply.setStatus(1);
			reply.setTicket(user.getTicket());
		}
		else {
			UUID uuid = UUID.randomUUID();
			String ticket = uuid.toString();
			
			user = new DataUser();
			user.setName( cmd.getName() );
			user.setHome( cmd.getHome() );
			user.setData_type( cmd.getDataType() );
			user.setReceive_url( cmd.getReceiveUrl() );
			user.setTicket(ticket);
			
			if( null == this.userRepo.save(user) )
			{
				reply.setStatus( -1 );
				reply.setTicket( "" );
			}
			else
			{
				reply.setStatus(0);
				reply.setTicket(ticket);
			}
		}
		rv = new ResponseEntity<>(reply, HttpStatus.OK);
		return rv;
	}
	
	@RequestMapping(value = "/api/users", method = GET)
    public List<DataUser> getUsers( ) {
		List<DataUser> rv;
		rv = this.userRepo.findAll();
		return rv;
    }
}
