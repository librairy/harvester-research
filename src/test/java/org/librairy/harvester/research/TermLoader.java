/*
 * Copyright (c) 2017. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research;

import com.google.common.base.CharMatcher;
import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.relations.AppearedIn;
import org.librairy.model.domain.relations.Relation;
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
import java.util.Optional;
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
public class TermLoader {

    private static final Logger LOG = LoggerFactory.getLogger(TermLoader.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;

    AtomicInteger counter = new AtomicInteger(0);


    @Test
    public void readTerms(){
        List<Resource> terms = udm.find(Resource.Type.TERM).all();
        LOG.info("Number of Terms: " + terms.size());


        for (Resource resource : terms){




            List<Relation> relation = udm.find(Relation.Type.APPEARED_IN).btw(resource.getUri(),
                    "http://drinventor.eu/domains/default");
            LOG.info("Relation: " + relation);

            Optional<Relation> appearedIn = udm.read(Relation.Type.APPEARED_IN).byUri(relation.get(0).getUri());
            LOG.info("AppearedIn: " + appearedIn.get().asAppearedIn());
        }


//
//        List<Relation> relations = udm.find(Relation.Type.APPEARED_IN).all();
//        LOG.info("Number of relations: " + relations.size());
//
//
//        for(Relation relation: relations){
//            AppearedIn appearedIn = relation.asAppearedIn();
//            LOG.info("AppearedIn: " + appearedIn);
//        }

    }


    @Test
    public void loadFromCSV(){

        uriGenerator.setBase("http://drinventor.eu/");

        List<TermItem> terms = processInputFile
                ("/Users/cbadenes/Documents/OEG/Projects/DrInventor/terms/terms-normalized.csv");

        LOG.info("Number of terms: " + terms.size());

        for(TermItem term: terms.subList(0,10)){
            Term termResource = new Term();
            termResource.setUri(uriGenerator.basedOnContent(Resource.Type.TERM, term.getTerm()));
            termResource.setCreationTime("2016-10-11T14:48+0200");
            termResource.setContent(term.getTerm());
//            udm.save(termResource);

            AppearedIn relation = new AppearedIn();
            relation.setCreationTime("2016-10-11T15:16+0200");
            relation.setStartUri(termResource.getUri());
            relation.setEndUri("http://drinventor.eu/domains/default");
            relation.setConsensus(term.getConsensus());
            relation.setCvalue(term.getCvalue());
            relation.setPertinence(term.getPertinence());
            relation.setProbability(term.getProbability());
            relation.setTermhood(term.getTermhood());
//            udm.save(relation);

            LOG.info("Term: " + relation);
        }


    }


    private List<TermItem> processInputFile(String inputFilePath) {
        List<TermItem> inputList = new ArrayList<TermItem>();
        try{
            File inputF = new File(inputFilePath);
            InputStream inputFS = new FileInputStream(inputF);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
            // skip the header of the csv
            inputList = br.lines().skip(1).map(mapToItem).filter(t -> isValid(t.getTerm())).collect(Collectors.toList
                    ());
            br.close();
        } catch (IOException e) {
            LOG.error("Error",e);
        }
        return inputList ;
    }

    private Function<String, TermItem> mapToItem = (line) -> {
        String[] p = line.split(";");// a CSV has comma separated lines
        if (p.length != 6) return new TermItem();
        TermItem item = new TermItem();
        item.setTerm(p[0]);
        item.setCvalue(Double.valueOf(p[1]));
        item.setConsensus(Double.valueOf(p[2]));
        item.setPertinence(Double.valueOf(p[3]));
        item.setTermhood(Double.valueOf(p[4]));
        item.setProbability(Double.valueOf(p[5]));
        //more initialization goes here
        return item;
    };


    private boolean isValid(String term){
        if (!CharMatcher.JAVA_LETTER.matchesAnyOf(term)) return false;

        if (term.length() > 25) return false;

        if (term.length() < 2) return false;


        return true;
    }

}
