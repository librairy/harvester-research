package org.librairy.harvester.research.processor;

import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;
import edu.upf.taln.dri.lib.Factory;
import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.loader.PDFloaderImpl;
import edu.upf.taln.dri.lib.model.Document;
import lombok.Setter;
import org.librairy.harvester.research.data.AnnotatedPaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Created on 22/04/16:
 *
 * @author cbadenes
 */
@Component
public class GateProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(GateProcessor.class);

    @Setter
    @Value("${librairy.upf.miner.config}")
    File driConfigPath;

    @Setter
    @Value("${librairy.upf.miner.proxy}")
    Boolean proxyEnabled;

    private LoadingCache<String, AnnotatedPaper> cache;

    @PostConstruct
    public void setup() throws DRIexception {

        LOG.info("Initializing UPF Text Mining Framework from: " + driConfigPath + " ..");

        // Enable the PDFX proxy service
        //PDFloaderImpl.PDFXproxyEnabled = true;
        PDFloaderImpl.PDFXproxyEnabled = proxyEnabled;

        // Set property file path
        Factory.setDRIPropertyFilePath(driConfigPath.getAbsolutePath());

        // Enable bibliography entry parsing
        Factory.setEnableBibEntryParsing(false);

        // Initialize
        Factory.initFramework();

        LOG.info("UPF Text Mining Framework initialized successfully");
    }



    public Document process(File file){

        LOG.info("Trying to compose a UPF-Gate file from: " + file.getAbsolutePath());
        Instant start           = Instant.now();
        String filePath         = file.getAbsolutePath();
        String extension        = Files.getFileExtension(file.getAbsolutePath()).toLowerCase();
        Document document       = null;
        try {
            switch (extension) {
                case "pdf":
                    LOG.info("parsing document as PDF document: " + filePath);
                    document = Factory.getPDFloader().parsePDF(filePath);
                    break;
                case "xml":
                case "htm":
                case "html":
                    LOG.info("parsing document as structured document: " + filePath);
                    document = Factory.createNewDocument(filePath);
                    break;
                default:
                    LOG.info("parsing document as plain text document: " + filePath);
                    document = Factory.getPlainTextLoader()
                            .parsePlainText(new
                                    File(filePath));
                    break;
            }
            Instant end = Instant.now();
            LOG.info("File '"+file.getName()+ "' composed successfully as UPF-Gate file in: " + ChronoUnit
                    .MINUTES.between(start,end) + "min " + ChronoUnit.SECONDS
                    .between(start,end) + "secs");

            return document;

        } catch (Exception e) {
            throw new RuntimeException("Error processing file by Gate: " + file.getAbsolutePath(), e);
        }


    }

}
