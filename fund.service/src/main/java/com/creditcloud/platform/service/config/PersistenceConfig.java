package com.creditcloud.platform.service.config;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;


import org.hibernate.dialect.MySQLDialect;
//import org.hibernate.dialect.MySQLDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(basePackages = "com.creditcloud.platform.service.repositories")
public class PersistenceConfig {
	@Bean
    public JpaVendorAdapter jpaVendorAdapter(final Environment environment) {
		final HibernateJpaVendorAdapter rv = new HibernateJpaVendorAdapter();
	
		//rv.setDatabase(Database.H2);
		//rv.setDatabasePlatform(H2Dialect.class.getName());
		rv.setDatabase(Database.MYSQL);
		rv.setDatabasePlatform(MySQLDialect.class.getName());
		//rv.setDatabasePlatform("MYSQL");
		rv.setGenerateDdl(environment.acceptsProfiles("dev"));
		rv.setShowSql(environment.acceptsProfiles("dev", "test"));
	
		return rv;
    }
	
	@Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory);
		return transactionManager;
    }
	
	@Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final Environment environment, final DataSource dataSource, final JpaVendorAdapter jpaVendorAdapter) {
		final Map<String, String> properties = new HashMap<>();
		properties.put("hibernate.generate_statistics", "false");
		if (environment.acceptsProfiles("dev")) {
		    properties.put("hibernate.hbm2ddl.auto", "update");
		}
	
		final LocalContainerEntityManagerFactoryBean rv = new LocalContainerEntityManagerFactoryBean();
		rv.setPersistenceUnitName("com.creditcloud.platform.service.entities_resourceLocale1");
		rv.setPackagesToScan("com.creditcloud.platform.service.entities");
		rv.setJpaDialect(new HibernateJpaDialect());
		rv.setJpaVendorAdapter(jpaVendorAdapter);
		rv.setDataSource(dataSource);
		rv.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
		rv.setJpaPropertyMap(properties);
		return rv;
    }    
}
