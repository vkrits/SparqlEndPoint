package org.forth.ics.isl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SparQL Endpoint client application for blazegraph using SpringMVC and AngulaJS
 * 
 * @author Vangelis Kritsotakis
 */

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

}