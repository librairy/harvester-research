/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research.processor;

import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;
import edu.upf.taln.dri.lib.Factory;
import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.model.Document;
import edu.upf.taln.dri.lib.util.ModuleConfig;
import edu.upf.taln.dri.lib.util.PDFtoTextConvMethod;
import lombok.Setter;
import org.librairy.harvester.research.data.AnnotatedPaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Created on 22/04/16:
 *
 * @author cbadenes
 */
//@Component
public class GateProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(GateProcessor.class);

    @Setter
    @Value("${librairy.upf.miner.config}")
    File driConfigPath;

    @Setter
    @Value("${LIBRAIRY_UPF_PROXY:true}")
    Boolean proxyEnabled;

    private LoadingCache<String, AnnotatedPaper> cache;

    @PostConstruct
    public void setup() throws DRIexception {

        LOG.info("Initializing UPF Text Mining Framework from: " + driConfigPath + " ..");




        // Enable the PDFX proxy service
        //PDFloaderImpl.PDFXproxyEnabled = true;
//        PDFloaderImpl.PDFXproxyEnabled = proxyEnabled;

        // Set property file path
        Factory.setDRIPropertyFilePath(driConfigPath.getAbsolutePath());

        // Enable bibliography entry parsing
//        Factory.setEnableBibEntryParsing(false);

        // -> New Section
        // To use PDFX:
//        Factory.setPDFtoTextConverter(PDFtoTextConvMethod.PDFX);

        // To use GROBID:
        Factory.setPDFtoTextConverter(PDFtoTextConvMethod.GROBID);

        // Instantiate the ModuleConfig class - the constructor sets all modules enabled by default
        ModuleConfig modConfigurationObj = new ModuleConfig();

        // Enable the parsing of bibliographic entries by means of online services (Bibsonomy, CrossRef, FreeCite, etc.)
        modConfigurationObj.setEnableBibEntryParsing(false);

        // Enable BabelNet Word Sense Disambiguation and Entity Linking over the text of the paper
        modConfigurationObj.setEnableBabelNetParsing(false);

        // Enable the parsing of the information from the header of the paper by means of online services (Bibsonomy, CrossRef, FreeCite, etc.)
        modConfigurationObj.setEnableHeaderParsing(true);

        // Enable the extraction of candidate terms from the sentences of the paper
        modConfigurationObj.setEnableTerminologyParsing(false);

        // Enable the dependency parsing of the sentences of a paper
        modConfigurationObj.setEnableGraphParsing(true);

        // Enable coreference resolution
        modConfigurationObj.setEnableCoreferenceResolution(false);

        // Enable the extraction of causal relations
        modConfigurationObj.setEnableCausalityParsing(true);

        // Enable the association of a rhetorical category to the sentences of the paper
        modConfigurationObj.setEnableRhetoricalClassification(true);

        // Import the configuration parameters set in the ModuleConfig instance
        Factory.setModuleConfig(modConfigurationObj);

        // -> New Section

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

            if (document == null) throw new IOException("Text Mining library error");

            LOG.info("File '"+file.getName()+ "' composed successfully as UPF-Gate file in: " + ChronoUnit
                    .MINUTES.between(start,end) + "min " + ChronoUnit.SECONDS
                    .between(start,end) + "secs");

            return document;

        } catch (Exception e) {
            throw new RuntimeException("Error processing file by Gate: " + file.getAbsolutePath(), e);
        }


    }

}
