/*
 * Copyright (c) 2017. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research.siggraph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.cbadenes.lab.test.IntegrationTest;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.impl.common.Levenshtein;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.harvester.research.TestConfig;
import org.librairy.model.domain.relations.Bundles;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
        "librairy.uri = drinventor.eu"
})
public class RetrievePapers {

    private static final Logger LOG = LoggerFactory.getLogger(RetrievePapers.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;

    AtomicInteger counter = new AtomicInteger(0);


    @Test
    public void getIds() throws UnirestException, IOException {

        File jsonFile = Paths.get("/Users/cbadenes/Documents/OEG/Projects/DrInventor/datasets/siggraph-2016" +
                "/meta2hui.json").toFile();


        ObjectMapper jsonMapper = new ObjectMapper();

        Paper[] papers = jsonMapper.readValue(jsonFile, Paper[].class);

        for (Paper paper: papers){

            LOG.info("Paper Title: " + paper.getTitle());

            String searchKey = URLEncoder.encode(paper.getTitle(), "UTF-8");

            LOG.info("Search Key: " + searchKey);

            HttpResponse<JsonNode> response = Unirest.get
                    ("https://helloacm" +
                            ".com/DRISurvey/api/searchPapersServlet/?key=" + searchKey + "&_=1493410082927")
                    .header("Host", "helloacm.com")
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:53.0) Gecko/20100101 " +
                            "Firefox/53.0")
                    .header("Accept", "application/json")
                    .header("Accept-Language", "es-ES,en-US;q=0.7,en;q=0.3")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Referer", "https://helloacm.com/DRISurvey/search_new/?userId=cbadenes")
                    .header("Cookie", "__cfduid=d80ca28442f52655baa5a145d1c9034231493409939")
                    .asJson();


            Suggestion[] suggestions = jsonMapper.readValue(response.getBody().toString(), Suggestion[].class);


            for (Suggestion suggestion: suggestions){

                int distance = Levenshtein.distance(paper.getTitle(), suggestion.getTitle());

                LOG.info("["+distance+"] - " + suggestion.getTitle());

                if (distance < 2) {
                    paper.setId(suggestion.getId());
                    break;
                }

                LOG.error("Not found: " + paper.getTitle());
                break;

            }
            LOG.info("==============");
        }


        File outJsonFile = Paths.get("/Users/cbadenes/Documents/OEG/Projects/DrInventor/datasets/siggraph-2016" +
                "/meta2hui_indexed.json").toFile();

        if (!outJsonFile.exists()) outJsonFile.createNewFile();

        jsonMapper.writeValue(outJsonFile, papers);

        /**
         * [{"conference":"ACM-SIGGRAPH","year":"2006","author":"","id":"6e93268d_2c05_43f8_9b64_0946f303b5a1","title":"A spatial data structure for fast Poisson-disk sample generation"}]
         */

    }


    @Test
    public void insertToSystem() throws IOException {
        File outJsonFile = Paths.get("/Users/cbadenes/Documents/OEG/Projects/DrInventor/datasets/siggraph-2016/meta2hui_indexed.json").toFile();

        ObjectMapper jsonMapper = new ObjectMapper();

        Paper[] papers = jsonMapper.readValue(outJsonFile, Paper[].class);

        for (Paper paper: papers){

            // Save Document
            Document document = new Document();
            document.setUri("http://drinventor.eu/documents/" + paper.getId());
            document.setCreationTime("2017-02-01T13:30+0200");
            document.setAuthoredBy(paper.getAuthor());
            document.setAuthoredOn(paper.getDate());
            document.setPublishedOn("2016");
            document.setPublishedBy("siggraph");
            document.setRetrievedFrom("http://drinventor.ccgv.org.uk/db-siggraph/"+paper.getId()+"/Full.pdf");
            document.setRetrievedOn("2017-02-01T13:30+0200");
            document.setFormat("pdf");
            document.setLanguage("en");
            document.setTitle(paper.getTitle());
            document.setDescription(paper.getAbstract());
//            udm.save(document);
            LOG.info("Document saved! " + document);


            // Save Item
            Item item = new Item();
            item.setAuthoredBy(paper.getAuthor());
            item.setLanguage("en");
            item.setCreationTime("2017-02-01T13:30+0200");
            //TODO read pdf!!
            String content = new String(Files.readAllBytes(Paths.get("/Users/cbadenes/Documents/OEG/Projects/DrInventor/datasets/siggraph-2016/sig2016",
                    StringUtils.replace(paper.getPdfFile(), ".pdf", ".txt"))));
            item.setContent(content);
            item.setType("text");
//            udm.save(item);
            LOG.info("Item saved! " + item);



            // Save Relation
            Bundles bundles = new Bundles();
            bundles.setWeight(1.0);
            bundles.setCreationTime(document.getCreationTime());
            bundles.setStartUri(document.getUri());
            bundles.setEndUri(item.getUri());
//            udm.save(bundles);
            LOG.info("Bundle saved! " + bundles);


            // Get Topic Distribution


            // Save DealsWith Item


            // Save DealsWith Document


        }



    }

}
