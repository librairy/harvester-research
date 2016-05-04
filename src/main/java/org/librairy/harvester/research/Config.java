package org.librairy.harvester.research;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Created by cbadenes on 01/12/15.
 */
@Configuration("harvester-research")
@ComponentScan({"org.librairy"})
@PropertySource({"classpath:harvester.properties","classpath:harvesterResearch.properties","classpath:boot.properties"})
public class Config {


    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
