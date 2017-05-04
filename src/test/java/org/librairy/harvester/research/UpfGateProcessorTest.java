/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research;

import com.google.common.base.Strings;
import edu.upf.taln.dri.lib.Factory;
import edu.upf.taln.dri.lib.exception.DRIexception;
import es.cbadenes.lab.test.IntegrationTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.harvester.research.data.AnnotatedPaper;
import org.librairy.harvester.research.processor.DocumentProcessor;
import org.librairy.harvester.research.processor.GateProcessor;
import org.librairy.harvester.research.processor.UpfProcessor;
import org.librairy.model.domain.relations.Describes;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Part;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 20/04/16:
 *
 * @author cbadenes
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
public class UpfGateProcessorTest {

    private static final Logger LOG = LoggerFactory.getLogger(UpfGateProcessorTest.class);

    @Autowired
    UDM udm;

    AtomicInteger counter = new AtomicInteger(0);

    Integer totalSize = 0;

//    @Test
//    public void analyzePdf(){
//
//        try {
//
//            GateProcessor gateProcessor = new GateProcessor();
//            URL url = this.getClass().getResource("/DRIconfig.properties");
//            gateProcessor.setDriConfigPath(new File(url.getFile()));
//            gateProcessor.setProxyEnabled(true);
//            gateProcessor.setup();
//
//
//            UpfProcessor upfProcessor = new UpfProcessor();
//
//
//            DocumentProcessor documentProcessor = new DocumentProcessor();
//            documentProcessor.setGateProcessor(gateProcessor);
//            documentProcessor.setUpfProcessor(upfProcessor);
//            documentProcessor.setup();
//
//
//            AnnotatedPaper document = documentProcessor.process(new File
//                    ("src/test/resources/workspace/default/p473-kovar.pdf")
//                    .getAbsolutePath());
//
//            LOG.info("Annotated Document: " + document.getTitle());
//
//
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (DRIexception drIexception) {
//            drIexception.printStackTrace();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }

    public void parse(UpfProcessor processor, String uri){

        try {

            LOG.info("Parsing " + counter.getAndIncrement() + " / " + totalSize);

            Optional<Resource> result = udm.read(Resource.Type.DOCUMENT).byUri(uri);


            if (result.isPresent()){

                Document document = result.get().asDocument();


                List<Resource> parts = udm.find(Resource.Type.PART).from(Resource.Type.DOCUMENT, document.getUri());


                if (parts == null || parts.isEmpty()){

                    List<Resource> items = udm.find(Resource.Type.ITEM).from(Resource.Type.DOCUMENT, document.getUri());

                    if (items == null || items.isEmpty()){
                        LOG.error("No item found by document: " + document.getUri());
                        return;
                    }

                    String itemUri = items.get(0).getUri();


                    edu.upf.taln.dri.lib.model.Document gateDoc = Factory.getPDFloader().parsePDF(new URL(document.getRetrievedFrom()));
                    AnnotatedPaper annotatedDoc = processor.process(gateDoc);

                    // abstract
                    String abstractContent = annotatedDoc.getSections().get("abstract");
                    if (!Strings.isNullOrEmpty(abstractContent))
                        savePart("abstract",abstractContent, itemUri);

                    Map<String, String> rethoricalClasses = annotatedDoc.getRhetoricalClasses();

                    List<String> sections = Arrays.asList(new String[]{
                            "challenge", "background", "approach", "outcome", "futureWork"
                    });


                    for (String section: sections){
                        if (rethoricalClasses.containsKey(section)){
                            savePart(section,rethoricalClasses.get(section),itemUri);
                        }
                    }
                }

                if (Strings.isNullOrEmpty(document.getDescription()) || Strings.isNullOrEmpty(document.getAuthoredBy())){

                    document.setDescription("-");
                    document.setAuthoredBy("-");
                    udm.save(document);

                    // Check if exists
                    String fileName = StringUtils.substringAfterLast(document.getUri(), "/") + ".pdf";
                    File pdfFile = Paths.get("documents",fileName).toFile();


                    edu.upf.taln.dri.lib.model.Document gateDoc;
                    if (pdfFile.exists()){
                        LOG.info("Document already downloaded: " + document.getUri());
                        gateDoc = Factory.getPDFloader().parsePDF(pdfFile.getAbsolutePath());
                    }else{
                        gateDoc = Factory.getPDFloader().parsePDF(new URL(document.getRetrievedFrom()));
                    }


                    AnnotatedPaper annotatedDoc = processor.process(gateDoc);

                    if (!Strings.isNullOrEmpty(annotatedDoc.getTitle())) document.setTitle(annotatedDoc.getTitle());

                    String abstractContent = annotatedDoc.getSections().get("abstract");
                    if (!Strings.isNullOrEmpty(abstractContent))
                        document.setDescription(abstractContent);
                    else document.setDescription(annotatedDoc.getSummary());


                    String authors = annotatedDoc.getAuthors();
                    if (!Strings.isNullOrEmpty(authors))
                        document.setAuthoredBy(authors);
                    else if (Strings.isNullOrEmpty(document.getAuthoredBy())){
                        document.setAuthoredBy("unknown");
                    }

                    udm.save(document);
                }

            }

        } catch (DRIexception drIexception) {
            drIexception.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void savePart(String sense, String content, String itemUri){
        Part part = new Part();
        part.setContent(content);
        part.setSense(sense);
        part.setCreationTime("2017-02-01T13:30+0200");
        udm.save(part);

        Describes describes = new Describes();
        describes.setCreationTime("2017-02-01T13:30+0200");
        describes.setEndUri(itemUri);
        describes.setStartUri(part.getUri());
        describes.setWeight(1.0);
        udm.save(describes);


    }

    @Test
    public void annotateDocuments(){

        try {
            System.out.println("starting test");
            LOG.info("Preparing Gate processor...");
            GateProcessor gateProcessor = new GateProcessor();
            URL url = this.getClass().getResource("/DRIconfig.properties");
            gateProcessor.setDriConfigPath(new File(url.getFile()));
            gateProcessor.setProxyEnabled(true);
            gateProcessor.setup();
            UpfProcessor upfProcessor = new UpfProcessor();


//            DocumentProcessor documentProcessor = new DocumentProcessor();
//            documentProcessor.setGateProcessor(gateProcessor);
//            documentProcessor.setUpfProcessor(upfProcessor);
//            documentProcessor.setup();

            // Documents
            List<Resource> documents = udm.find(Resource.Type.DOCUMENT).all();
            totalSize = documents.size();
            LOG.info(documents.size() + " documents");

            documents.parallelStream().forEach( resource -> parse(upfProcessor, resource.getUri()));

            LOG.info("Operation Completed Successfully!!");

        } catch (DRIexception drIexception) {
            drIexception.printStackTrace();
        }
    }

    @Test
    public void annotateDocument(){
        try {
            System.out.println("starting test");
            LOG.info("Preparing Gate processor...");
            GateProcessor gateProcessor = new GateProcessor();
            URL url = this.getClass().getResource("/DRIconfig.properties");
            gateProcessor.setDriConfigPath(new File(url.getFile()));
            gateProcessor.setProxyEnabled(true);
            gateProcessor.setup();
            UpfProcessor upfProcessor = new UpfProcessor();

            String uri = "http://drinventor.eu/documents/bd074764_cf3f_4656_a8ac_808873f156ce";

            parse(upfProcessor, uri);

            LOG.info("Operation Completed Successfully!!");

        } catch (DRIexception drIexception) {
            drIexception.printStackTrace();
        }
    }


    @Test
    public void evaluateDocuments(){

        // Documents
        List<Resource> documents = udm.find(Resource.Type.DOCUMENT).all();
        totalSize = documents.size();
        LOG.info(documents.size() + " documents");

        AtomicInteger counterMissingDescription = new AtomicInteger(0);
        AtomicInteger counterMissingAuthors = new AtomicInteger(0);
        documents.parallelStream().forEach( resource -> {

            Optional<Resource> doc = udm.read(Resource.Type.DOCUMENT).byUri(resource.getUri());

            if (doc.isPresent()){
                String uri = resource.getUri();
                if( Strings.isNullOrEmpty(doc.get().asDocument().getDescription())) {
                    counterMissingDescription.getAndIncrement();
//                    LOG.warn("Document Missing Description: " + StringUtils.replace(uri,"drinventor.eu","drinventor" +
//                            ".dia.fi.upm.es"));
                }else{
                    LOG.info("Document Description: " + StringUtils.replace(uri,"drinventor.eu","drinventor.dia.fi.upm.es"));
                }
                if( Strings.isNullOrEmpty(doc.get().asDocument().getAuthoredBy())) {
                    counterMissingAuthors.getAndIncrement();
//                    LOG.info("Document Missing Authors: " + StringUtils.replace(uri,"drinventor.eu","drinventor.dia.fi.upm.es"));
                }

            }

        });


        LOG.info("Missing Descriptions: "   + counterMissingDescription.get() + " of " + totalSize );
        LOG.info("Missing Authors: "        + counterMissingAuthors.get() + " of " + totalSize );

    }

    @Test
    public void downloadDocuments(){

        File folder = Paths.get("documents").toFile();

        if (!folder.exists()){
            folder.mkdirs();
        }

        // Documents
        List<Resource> documents = udm.find(Resource.Type.DOCUMENT).all();
        totalSize = documents.size();
        LOG.info(documents.size() + " documents");

        AtomicInteger counter = new AtomicInteger(0);
        documents.parallelStream().forEach( resource -> {

            Optional<Resource> doc = udm.read(Resource.Type.DOCUMENT).byUri(resource.getUri());

            if (doc.isPresent()){

                Document document = doc.get().asDocument();
                String fileName = StringUtils.substringAfterLast(document.getUri(), "/") + ".pdf";
                Path pdfFile = Paths.get(folder.getPath(), fileName);

                if (!pdfFile.toFile().exists()){
                    try {
                        FileUtils.copyURLToFile(new URL(document.getRetrievedFrom()),pdfFile.toFile());
                        LOG.info(document.getRetrievedFrom() + " downloaded!");
                        counter.getAndIncrement();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        });


        LOG.info("Total Documents: "    + totalSize );
        LOG.info("Download Documents: " + counter );

    }

    @Test
    public void fixDescriptions(){

        // Documents
        List<Resource> documents = udm.find(Resource.Type.DOCUMENT).all();
        totalSize = documents.size();
        LOG.info(documents.size() + " documents");
        AtomicInteger counter = new AtomicInteger(0);
        documents.parallelStream().forEach( resource -> {

            Optional<Resource> doc = udm.read(Resource.Type.DOCUMENT).byUri(resource.getUri());

            if (doc.isPresent()){

                Document document = doc.get().asDocument();

                if( !Strings.isNullOrEmpty(document.getDescription()) && document.getDescription().contains(". " +
                        "Abstract")) {
                    counter.getAndIncrement();


                    String description = StringUtils.substringAfter(document.getDescription(), ". Abstract");
                    document.setDescription(description);
                    udm.save(document);

                    String uri = resource.getUri();
                    LOG.info("Document Error Description: " + StringUtils.replace(uri,"drinventor.eu","drinventor.dia" +
                            ".fi.upm.es"));
                }

            }

        });

        LOG.info("Description Error: "        + counter.get() + " of " + totalSize );

    }

}
