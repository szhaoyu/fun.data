package com.creditcloud.platform.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.creditcloud.platform.service.entities.DataUser;

public interface DataUserRepository extends JpaRepository<DataUser, Integer> {
	public DataUser findByUrl( final @Param("url")  String url );
	public DataUser findByTicket( final @Param("ticket")  String ticket );
}
