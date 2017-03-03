package org.forth.ics.isl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * SparQL Endpoint client application for blazegraph using SpringMVC and AngulaJS
 * 
 * @author Vangelis Kritsotakis
 */

@SpringBootApplication
//public class Application extends SpringBootServletInitializer {
public class Application {
	
	/*
	 * Used only when deployed as war file on existing container
	 */
	/*
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
	*/
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

}

// Tomcat
//http://stackoverflow.com/questions/39187237/deploying-spring-boot-war-to-tomcat-8-http-404-when-accessing-resources