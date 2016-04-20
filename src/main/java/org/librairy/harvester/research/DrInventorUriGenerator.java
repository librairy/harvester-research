package org.librairy.harvester.research;

import org.librairy.storage.generator.URIGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created on 20/04/16:
 *
 * @author cbadenes
 */
@Component
public class DrInventorUriGenerator {

    @Autowired
    URIGenerator uriGenerator;

    @PostConstruct
    public void setup(){
        uriGenerator.setBase("http://drinventor.eu/");
    }
}
