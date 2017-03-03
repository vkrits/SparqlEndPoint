package org.forth.ics.isl.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MyWebMvcConfig extends WebMvcConfigurerAdapter {
	
	@Value("${rdf.afterbase}")
	private String rdfAfterbase;
	
	/**
     * Maps all AngularJS routes to index so that they work with direct linking.
     */
    @Controller
    static class Routes {

		@RequestMapping({ 
			"/query", 
			"/explore", 
			"/roles", 
			"/exploreInit/**",
			"/${rdf.afterbase}/**"
		})
		public String index() {
			return "forward:/index.html";
		}
	}
	/*
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		System.out.println("forward");
		// forward requests to /admin and /user to their index.html
		registry.addViewController("/" + rdfAfterbase).setViewName("forward:/index.html");
	}
	*/
}