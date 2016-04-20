package org.librairy.harvester.research;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;
import edu.upf.taln.dri.lib.Factory;
import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.loader.PDFloaderImpl;
import edu.upf.taln.dri.lib.model.Document;
import lombok.Setter;
import org.librairy.harvester.research.data.DocumentWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created on 20/04/16:
 *
 * @author cbadenes
 */
@Component
public class UpfGateProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(UpfGateProcessor.class);

    @Setter
    @Value("${librairy.upf.miner.config}")
    File driConfigPath;

    @Setter
    @Value("${librairy.upf.miner.proxy}")
    Boolean proxyEnabled;

    private LoadingCache<String, DocumentWrapper> cache;

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

        // Prepare Cache
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<String, DocumentWrapper>() {
                            public DocumentWrapper load(String filePath)  {

                                // Check if exists serialized
                                if (new File(filePath+".ser").exists()){
                                    return DocumentWrapper.deserialize(filePath+".ser");
                                }

                                // Analyze
                                String extension = Files.getFileExtension(filePath).toLowerCase();
                                DocumentWrapper documentWrapper = new DocumentWrapper();
                                try{
                                    switch(extension){
                                        case "pdf":
                                            LOG.info("parsing document as PDF document: " + filePath);
                                            documentWrapper =  new DocumentWrapper(Factory.getPDFloader().parsePDF
                                                    (filePath));
                                            break;
                                        case "xml":
                                        case "htm":
                                        case "html":
                                            LOG.info("parsing document as structured document: " + filePath);
                                            documentWrapper =  new DocumentWrapper(Factory.createNewDocument(filePath));
                                            break;
                                        default:
                                            LOG.info("parsing document as plain text document: " + filePath);
                                            documentWrapper =  new DocumentWrapper(Factory.getPlainTextLoader()
                                                    .parsePlainText(new
                                                            File(filePath)));
                                            break;
                                    }

                                    DocumentWrapper.serialize(documentWrapper,filePath+".ser");

                                } catch (DRIexception e){
                                    LOG.warn("Error parsing file: " + filePath, e);
                                }

                                return documentWrapper;



                            }
                        });

        LOG.info("UPF Text Mining Framework initialized successfully");
    }


    public DocumentWrapper process(String filePath) throws ExecutionException {
        LOG.info("Processing file: " + filePath);
        return this.cache.get(filePath);
//        return analyze(filePath);
    }


    private DocumentWrapper analyze(String filePath){
        // Check if exists serialized
        if (new File(filePath+".ser").exists()){
            return DocumentWrapper.deserialize(filePath+".ser");
        }

        // Analyze
        String extension = Files.getFileExtension(filePath).toLowerCase();
        Document document = null;
        DocumentWrapper documentWrapper = new DocumentWrapper();
        try{
            switch(extension){
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
                    document = Factory.getPlainTextLoader().parsePlainText(new File(filePath));
                    break;
            }
            documentWrapper.loadFrom(document);
            DocumentWrapper.serialize(documentWrapper,filePath+".ser");
            document.cleanUp();

        } catch (DRIexception e){
            LOG.warn("Error parsing file: " + filePath, e);
        }

        return documentWrapper;
    }
}
