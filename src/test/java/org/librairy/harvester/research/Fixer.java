/*
 * Copyright (c) 2017. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research;

import es.cbadenes.lab.test.IntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@TestPropertySource(properties = {
        "librairy.columndb.host             = wiener.dia.fi.upm.es",
        "librairy.documentdb.host           = wiener.dia.fi.upm.es",
        "librairy.graphdb.host              = wiener.dia.fi.upm.es",
        "librairy.eventbus.host             = wiener.dia.fi.upm.es",
        "librairy.topic = drinventor.eu"
})
public class Fixer {

    private static final Logger LOG = LoggerFactory.getLogger(Fixer.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;

    AtomicInteger counter = new AtomicInteger(0);


    @Test
    public void fixTitle(){

        List<Resource> documents = udm.find(Resource.Type.DOCUMENT).all();

        documents.parallelStream().forEach( res -> {
            Document doc = udm.read(Resource.Type.DOCUMENT).byUri(res.getUri()).get().asDocument();

            String title = doc.getTitle();
            String match = "(a) ";
            if (title.contains(match)){
                LOG.info("Title: " + title);
                doc.setTitle(StringUtils.substringBefore(title,match));
                udm.save(doc);
            }

        });


    }

}
