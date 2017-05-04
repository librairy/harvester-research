/*
 * Copyright (c) 2017. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research;

import com.google.common.base.CharMatcher;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.relations.AppearedIn;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Resource;
import org.librairy.model.domain.resources.Term;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
        "librairy.topic = drinventor.eu"
})
public class Report {

    private static final Logger LOG = LoggerFactory.getLogger(Report.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;

    AtomicInteger counter = new AtomicInteger(0);


    @Test
    public void summary(){
//
//        List<Relation> termsRel = udm.find(Relation.Type.APPEARED_IN).all();
//        long termsCount = termsRel.stream().map(rel -> udm.read(Relation.Type.APPEARED_IN).byUri(rel.getUri()).get().asAppearedIn())
//                .filter(ap -> ap.getTermhood() > 0.3).count();
//        LOG.info("Number of Relevant Terms: " + termsCount);

        List<Resource> documents = udm.find(Resource.Type.DOCUMENT).all();
        LOG.info("Number of Documents: " + documents.size());

        List<Relation> similarities = udm.find(Relation.Type.SIMILAR_TO_DOCUMENTS).all();
        LOG.info("Number of Similarities: " + similarities.size());

        List<Resource> terms = udm.find(Resource.Type.TERM).all();
        LOG.info("Number of Terms: " + terms.size());

        List<Resource> topics = udm.find(Resource.Type.TOPIC).all();
        LOG.info("Number of Topics: " + topics.size());

    }

    @Test
    public void documentDistributionPerYear(){

        List<Resource> documents = udm.find(Resource.Type.DOCUMENT).all();

        Map<String,AtomicInteger> counters = new ConcurrentHashMap<>();

        
        documents.parallelStream().forEach( res -> {
            Document doc = udm.read(Resource.Type.DOCUMENT).byUri(res.getUri()).get().asDocument();

            doc.getPublishedOn();
            AtomicInteger counter = new AtomicInteger(0);
            if (counters.containsKey(doc.getPublishedOn())){
                counter = counters.get(doc.getPublishedOn());
            }
            counter.getAndIncrement();
            counters.put(doc.getPublishedOn(), counter);

        });

        counters.entrySet().stream().sorted( (e1,e2) -> e1.getKey().compareTo(e2.getKey())).forEach( entry -> {
            //{ year: '2002', value: 64 },
            System.out.println("{ year: '" + entry.getKey() + "', value: " + entry.getValue().get() + " },");
        });
    }

    @Test
    public void documentShortList(){

        /**
         * <tr>
         <td>2002</td>
         <td><a href='http://drinventor.dia.fi.upm.es/api/0.2/documents/6ecefe140a55b8f1a73c975e65e1ecec'>Optimizing Continuity in Multiscale Imagery</a></td>
         <td>M. Egmont-Petersen, D. de Ridder, H. Handels</td>
         </tr>
         */

        List<Resource> documents = udm.find(Resource.Type.DOCUMENT).all();

        documents.stream().limit(10).forEach( res -> {
            Document doc = udm.read(Resource.Type.DOCUMENT).byUri(res.getUri()).get().asDocument();

            System.out.println("<tr>");
            System.out.println("<td>" + doc.getPublishedOn() + "</td>");

            String id = URIGenerator.retrieveId(doc.getUri());
            System.out.println("<td><a href='http://drinventor.dia.fi.upm.es/api/0.2/documents/" + id+"'>"+doc
                    .getTitle()+"</a></td>");
            System.out.println("<td>"+doc.getAuthoredBy()+"</td>");
            System.out.println("</tr>");

        });

    }

    @Test
    public void searchDocument(){

        String title = "Stress Relief";

        List<Resource> documents = udm.find(Resource.Type.DOCUMENT).all();

        documents.stream().forEach( res -> {
            Document doc = udm.read(Resource.Type.DOCUMENT).byUri(res.getUri()).get().asDocument();

            if (doc.getTitle().toLowerCase().contains(title.toLowerCase())){
                LOG.info("Title: " + doc.getTitle());
                LOG.info("URI: " + doc.getUri());
                LOG.info("=======");
            }

        });
    }

    @Test
    public void documentTopics(){

        String uri = "http://drinventor.eu/documents/bd074764_cf3f_4656_a8ac_808873f156ce";


        List<Relation> topics = udm.find(Relation.Type.DEALS_WITH_FROM_DOCUMENT).from(Resource.Type.DOCUMENT, uri);


        Integer index = 1;

        for (Relation rel: topics.stream().sorted((r1,r2) -> r1.getEndUri().compareTo(r2.getEndUri())).collect(Collectors.toList())){
            /**
             * {"name": "topics", "topic": "topic 1", "value": 0.2484},
             */
            String score = String.format ("%.3f", rel.getWeight());
            System.out.println("{\"name\": \"topics\", \"topic\": \"" + index++ + "\", \"value\": " + score + "},");
        }

    }

    @Test
    public void documentSimilar(){

        String uri = "http://drinventor.eu/documents/bd074764_cf3f_4656_a8ac_808873f156ce";


        List<Relation> similar = udm.find(Relation.Type.SIMILAR_TO_DOCUMENTS).from(Resource.Type.DOCUMENT, uri);


        Integer index = 1;

        for (Relation rel: similar.stream().sorted((r1,r2) -> -r1.getWeight().compareTo(r2.getWeight())).limit(7)
                .collect(Collectors.toList())){
            /**
             * {"name": "Interactive Manipulation of Large-Scale Crowd Animation", "similarity":0.99},
             */
            String score = String.format ("%.3f", rel.getWeight());
            Document doc = udm.read(Resource.Type.DOCUMENT).byUri(rel.getEndUri()).get().asDocument();
            System.out.println("{\"name\": \""+doc.getTitle()+"\", \"similarity\": " + score + "},");

        }

        for (Relation rel: similar.stream().sorted((r1,r2) -> -r1.getWeight().compareTo(r2.getWeight())).limit(7)
                .collect(Collectors.toList())){
            /**
             * {"source": "Stress Relief: Improving Structural Strength of 3D Printable Objects", "target": "Multimaterial Mesh-Based Surface Tracking" },
             */
            String score = String.format ("%.3f", rel.getWeight());
            Document doc = udm.read(Resource.Type.DOCUMENT).byUri(rel.getEndUri()).get().asDocument();
            System.out.println("{\"name\": \""+doc.getTitle()+"\", \"similarity\": " + score + "},");

        }

    }

    @Test
    public void getIds() throws UnirestException {
        String searchKey = "sample";

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
        System.out.println(response);
        System.out.println(response.getBody());

    }

}
