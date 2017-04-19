/*
 * Copyright (c) 2017. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

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
@ComponentScan({"org.librairy.storage", "org.librairy.annotation", "org.librairy.eventbus", "org.librairy.model","org.librairy.harvester.research"})
@PropertySource({"classpath:harvester.properties","classpath:harvesterResearch.properties","classpath:boot.properties"})
public class TestConfig {


    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
