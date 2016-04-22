package org.librairy.harvester.research;

import edu.upf.taln.dri.lib.exception.DRIexception;
import org.junit.Test;
import org.librairy.harvester.research.data.AnnotatedPaper;
import org.librairy.harvester.research.processor.DocumentProcessor;
import org.librairy.harvester.research.processor.GateProcessor;
import org.librairy.harvester.research.processor.UpfProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created on 20/04/16:
 *
 * @author cbadenes
 */
public class UpfGateProcessorTest {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentProcessor.class);

    @Test
    public void analyzePdf(){

        try {

            GateProcessor gateProcessor = new GateProcessor();
            URL url = this.getClass().getResource("/DRIconfig.properties");
            gateProcessor.setDriConfigPath(new File(url.getFile()));
            gateProcessor.setProxyEnabled(true);
            gateProcessor.setup();


            UpfProcessor upfProcessor = new UpfProcessor();


            DocumentProcessor documentProcessor = new DocumentProcessor();
            documentProcessor.setGateProcessor(gateProcessor);
            documentProcessor.setUpfProcessor(upfProcessor);
            documentProcessor.setup();


            AnnotatedPaper document = documentProcessor.process(new File
                    ("src/test/resources/workspace/default/p473-kovar.pdf")
                    .getAbsolutePath());

            LOG.info("Annotated Document: " + document.getTitle());


        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (DRIexception drIexception) {
            drIexception.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

}
