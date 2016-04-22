package org.librairy.harvester.research.processor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.model.Document;
import lombok.Setter;
import org.librairy.harvester.research.data.AnnotatedPaper;
import org.librairy.harvester.research.utils.Serializations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created on 20/04/16:
 *
 * @author cbadenes
 */
@Component
public class DocumentProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentProcessor.class);

    @Setter
    @Autowired
    GateProcessor gateProcessor;

    @Setter
    @Autowired
    UpfProcessor upfProcessor;

    private LoadingCache<String, AnnotatedPaper> cache;

    @PostConstruct
    public void setup() throws DRIexception {

        // Prepare Cache
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<String, AnnotatedPaper>() {
                            public AnnotatedPaper load(String filePath) {
                                return _process(filePath);

                            }
                        });

        LOG.info("Document processor initialized successfully");
    }


    public AnnotatedPaper process(String filePath) throws ExecutionException {
        return this.cache.get(filePath);
    }


    private AnnotatedPaper _process(String filePath){
        LOG.info("Processing file: " + filePath);

        Instant start = Instant.now();
        // Check if it was previously serialized
        if (new File(filePath + ".ser").exists()) {
            return Serializations.deserialize(AnnotatedPaper.class,filePath + ".ser");
        }

        // Process
        AnnotatedPaper annotatedPaper = new AnnotatedPaper();

        try {
            Document document = gateProcessor.process(new File(filePath));

            annotatedPaper = upfProcessor.process(document);

            Serializations.serialize(annotatedPaper,filePath + ".ser");

        } catch (Exception e) {
            LOG.warn("Error parsing file: " + filePath, e);
        }

        Instant end = Instant.now();
        LOG.info("File processed in: " + ChronoUnit.MINUTES.between(start,end) + "min " + ChronoUnit.SECONDS
                .between(start,end) + "secs");

        return annotatedPaper;
    }

}


