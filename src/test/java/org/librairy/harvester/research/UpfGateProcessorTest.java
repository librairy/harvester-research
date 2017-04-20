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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.harvester.research.data.AnnotatedPaper;
import org.librairy.harvester.research.processor.DocumentProcessor;
import org.librairy.harvester.research.processor.GateProcessor;
import org.librairy.harvester.research.processor.UpfProcessor;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
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

    private static final Logger LOG = LoggerFactory.getLogger(DocumentProcessor.class);

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

                if (Strings.isNullOrEmpty(document.getDescription())){
                    edu.upf.taln.dri.lib.model.Document gateDoc = Factory.getPDFloader().parsePDF(new URL(document.getRetrievedFrom()));
                    AnnotatedPaper annotatedDoc = processor.process(gateDoc);

                    if (!Strings.isNullOrEmpty(annotatedDoc.getTitle())) document.setTitle(annotatedDoc.getTitle());

                    String abstractContent = annotatedDoc.getSections().get("abstract");
                    if (!Strings.isNullOrEmpty(abstractContent)) document.setDescription(abstractContent);

                    udm.save(document);
                }

            }

        } catch (DRIexception drIexception) {
            drIexception.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

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

}
