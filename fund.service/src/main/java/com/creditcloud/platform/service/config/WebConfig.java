package com.creditcloud.platform.service.config;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule.Priority;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	/*
	@Bean
    public ServletContextInitializer servletContextInitializer() {
		return servletContext -> {
		    final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		    characterEncodingFilter.setEncoding("UTF-8");
		    characterEncodingFilter.setForceEncoding(false);
		    
		    servletContext.addFilter("characterEncodingFilter", characterEncodingFilter).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
		};
    }*/
	
	@Bean
    public MultipartConfigElement multipartConfigElement() {
		return new MultipartConfigElement("", 5 * 1024 * 1024, 5 * 1024 * 1024, 1024 * 1024);	
    }
	
	@Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		super.configureContentNegotiation(configurer);
		configurer.favorParameter(true);
    }
	
	@Override
    public void configurePathMatch(final PathMatchConfigurer configurer) {
		configurer.setUseRegisteredSuffixPatternMatch(true);
    }
	
	@Bean
    public ObjectMapper jacksonObjectMapper() {
	return new ObjectMapper().registerModules(
		new JaxbAnnotationModule().setPriority(Priority.SECONDARY)
	);
    }

    @Bean    
    public EmbeddedServletContainerCustomizer embeddedServletContainerCustomizer(
	    final @Value("${biking2.connector.proxyName:}") String proxyName,
	    final @Value("${biking2.connector.proxyPort:80}") int proxyPort
    ) {
	return (ConfigurableEmbeddedServletContainer configurableContainer) -> {
	    if (configurableContainer instanceof TomcatEmbeddedServletContainerFactory) {
		final TomcatEmbeddedServletContainerFactory containerFactory = (TomcatEmbeddedServletContainerFactory) configurableContainer;
		containerFactory.setTldSkip("*.jar");
		if(!proxyName.isEmpty()) {
		    containerFactory.addConnectorCustomizers(connector -> {
			connector.setProxyName(proxyName);
			connector.setProxyPort(proxyPort);
		    });
		}
	    }
	};
    }
}
