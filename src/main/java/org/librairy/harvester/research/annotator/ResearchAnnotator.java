package org.librairy.harvester.research.annotator;

import org.librairy.harvester.file.annotator.AnnotatedDocument;
import org.librairy.harvester.file.annotator.Annotator;
import org.librairy.harvester.research.UpfGateProcessor;
import org.librairy.harvester.research.data.DocumentWrapper;
import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Created on 20/04/16:
 *
 * @author cbadenes
 */
@Component
public class ResearchAnnotator implements Annotator{

    private static final Logger LOG = LoggerFactory.getLogger(ResearchAnnotator.class);

    @Autowired
    UpfGateProcessor processor;

    @Autowired
    UDM udm;

    @Override
    public AnnotatedDocument annotate(String itemURI) {

        LOG.debug("Trying to annotate item: " + itemURI);


        Optional<Resource> res = udm.read(Resource.Type.ITEM).byUri(itemURI);

        if (!res.isPresent()){
            throw new RuntimeException("No Item found by uri: " + itemURI);
        }

        AnnotatedDocument annotatedDocument = new AnnotatedDocument();
        try {
            Item item = res.get().asItem();



            DocumentWrapper document = processor.process(item.getUrl());

            // Retrieve rhetorical classes
            Map<String, String> rhetoricalClasses = new HashMap();
            rhetoricalClasses.put("abstract",document.getAbstractContent());
            rhetoricalClasses.put("approach",document.getApproachContent());
            rhetoricalClasses.put("background",document.getBackgroundContent());
            rhetoricalClasses.put("challenge",document.getChallengeContent());
            rhetoricalClasses.put("futureWork",document.getFutureWorkContent());
            rhetoricalClasses.put("outcome",document.getOutcomeContent());
            annotatedDocument.setRhetoricalClasses(rhetoricalClasses);

        } catch (ExecutionException e) {
            LOG.warn("Error annotating item: " + itemURI, e);
        }
        return annotatedDocument;
    }
}
