package org.librairy.harvester.research;

import edu.upf.taln.dri.lib.exception.DRIexception;
import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.harvester.research.data.DocumentWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created on 20/04/16:
 *
 * @author cbadenes
 */
public class UpfGateProcessorTest {

    @Test
    public void analyzePdf(){

        try {

            UpfGateProcessor upfGateProcessor = new UpfGateProcessor();

            URL url = this.getClass().getResource("/DRIconfig.properties");
            upfGateProcessor.setDriConfigPath(new File(url.getFile()));
            upfGateProcessor.setProxyEnabled(true);
            upfGateProcessor.setup();

            DocumentWrapper res = upfGateProcessor.process(new File
                    ("src/test/resources/workspace/default/p473-kovar.pdf")
                    .getAbsolutePath());

            System.out.println(res);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (DRIexception drIexception) {
            drIexception.printStackTrace();
        }

    }

}
