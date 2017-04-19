/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research.annotator;

import org.librairy.harvester.file.annotator.AnnotatedDocument;
import org.librairy.harvester.file.annotator.Annotator;
import org.librairy.harvester.research.data.AnnotatedPaper;
import org.librairy.harvester.research.processor.DocumentProcessor;
import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Created on 20/04/16:
 *
 * @author cbadenes
 */
//@Component
public class ResearchAnnotator implements Annotator{

    private static final Logger LOG = LoggerFactory.getLogger(ResearchAnnotator.class);

    @Autowired
    DocumentProcessor documentProcessor;

    @Autowired
    UDM udm;

    @Override
    public AnnotatedDocument annotate(String itemURI) {

        LOG.info("Trying to annotate item: " + itemURI);
        Optional<Resource> res = udm.read(Resource.Type.ITEM).byUri(itemURI);

        if (!res.isPresent()){
            throw new RuntimeException("No Item found by uri: " + itemURI);
        }

        AnnotatedDocument annotatedDocument = new AnnotatedDocument();
        try {
            Item item = res.get().asItem();

            AnnotatedPaper annotatedPaper = documentProcessor.process(item.getUrl());

            // Retrieve rhetorical classes
            annotatedDocument.setRhetoricalClasses(annotatedPaper.getRhetoricalClasses());

        } catch (ExecutionException e) {
            LOG.warn("Error annotating item: " + itemURI, e);
        }
        LOG.info("Annotation completed for item: " + itemURI);
        return annotatedDocument;
    }
}
