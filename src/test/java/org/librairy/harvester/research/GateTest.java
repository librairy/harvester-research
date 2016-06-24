package org.librairy.harvester.research;

import edu.upf.taln.dri.lib.Factory;
import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.loader.PDFloaderImpl;
import edu.upf.taln.dri.lib.model.DocumentImpl;
import edu.upf.taln.dri.lib.model.ext.Sentence;
import edu.upf.taln.dri.lib.model.ext.SentenceSelectorENUM;
import edu.upf.taln.dri.lib.model.graph.DocGraphTypeENUM;
import edu.upf.taln.dri.lib.model.util.DocParse;
import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.List;

/**
 * Created on 26/04/16:
 *
 * @author cbadenes
 */
@Category(IntegrationTest.class)
public class GateTest {

    @Test
    public void parse(){
        // Property file path
        URL url = this.getClass().getResource("/DRIconfig.properties");
        Factory.setDRIPropertyFilePath(url.getPath());
        Factory.setEnableBibEntryParsing(false);
        PDFloaderImpl.PDFXproxyEnabled = true;

        // Loading a paper from a locally stored PDF file at the path: /my/file/path/PDF_file_name.pdf and get the Dr. Inventor Document instance
        String paperFileName = "p473-kovar.pdf"; // paper_45.pdf
        String PDF_FILE_PATH = "src/test/resources/workspace/default/" + paperFileName;

        try {

            long start = System.currentTimeMillis();

            DocumentImpl doc_PDFpaperFILE = (DocumentImpl) Factory.getPDFloader().parsePDF(PDF_FILE_PATH);

            doc_PDFpaperFILE.preprocess();

            System.out.println("********** PAPER CONVERTED AND PROCESSED IN: " + (System.currentTimeMillis() - start) + " ms.");

            String srtorageFileName = paperFileName.replace(".pdf", "") + "_1.3.xml";

            try {
                // Store File
                File file = new File("src/test/resources/workspace/default/" + srtorageFileName);

                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(doc_PDFpaperFILE.getXMLString());
                bw.flush();
                bw.close();


                System.out.println("File stored to: " + file.getAbsolutePath());
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            DocumentImpl loadedDoc = null;
            try {
                loadedDoc = (DocumentImpl) Factory.createNewDocument("src/test/resources/workspace/default/" + srtorageFileName);
            } catch (DRIexception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                List<Sentence> abstractSenList = loadedDoc.extractSentences(SentenceSelectorENUM.ONLY_ABSTRACT);
                int i = 0;
                for(Sentence sent : abstractSenList) {
                    if(sent != null) {
                        i++;
                        System.out.println(i + " > ABSTRACT - " + sent.getRhetoricalClass() + " - " + sent.getText());
                    }
                }

                List<Sentence> contentSenList = loadedDoc.extractSentences(SentenceSelectorENUM.ALL_EXCEPT_ABSTRACT);
                for(Sentence sent : contentSenList) {
                    if(sent != null) {
                        i++;
                        System.out.println(i + " > CONTENT - " + sent.getRhetoricalClass() + " - " + sent.getText());
                    }
                }
            } catch (InternalProcessingException e) {
                e.printStackTrace();
            }

            start = System.currentTimeMillis();
            System.out.println(DocParse.getTokenROSasCSVstring(loadedDoc, SentenceSelectorENUM.ALL, DocGraphTypeENUM.DEP));
            System.out.println("********** CSV GENERATED IN: " + (System.currentTimeMillis() - start) + " ms.");
            System.out.println(DocParse.getDocumentROSasCSVstring(loadedDoc, SentenceSelectorENUM.ALL));
            System.out.println("********** CSV GENERATED IN: " + (System.currentTimeMillis() - start) + " ms.");
            System.out.println("\n\n\n*************************\n\n\n");


        } catch (DRIexception e) {
            e.printStackTrace();
        }
    }
}
