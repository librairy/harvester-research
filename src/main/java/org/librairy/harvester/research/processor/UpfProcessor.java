package org.librairy.harvester.research.processor;

import edu.upf.taln.dri.lib.model.Document;
import edu.upf.taln.dri.lib.model.ext.*;
import org.librairy.harvester.research.data.AnnotatedPaper;
import org.librairy.harvester.research.data.AuthorWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 22/04/16:
 *
 * @author cbadenes
 */
@Component
public class UpfProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(UpfProcessor.class);

    public AnnotatedPaper process(Document document){

        LOG.info("Trying to retrieve information from a Upf-Gate Document: " + document);
        Instant start           = Instant.now();
        AnnotatedPaper annotatedPaper = new AnnotatedPaper();

        try {
            //Pre-load elements
            document.preprocess();

            // Content
            annotatedPaper.setContent(document.getRawText());

            // Header
            Header header = document.extractHeader();

            // ->   Title
            annotatedPaper.setTitle(header.getTitle().trim());

            // ->   DOI
            annotatedPaper.setDoi(header.getDoi());

            // ->   Year
            annotatedPaper.setYear(header.getYear());

            // ->   Authors
            annotatedPaper.setAuthors(header.getAuthorList().stream().map(AuthorWrapper::new).map(author -> author.getFullName())
                    .collect(Collectors.joining(", ")));

            // Sections
            List<Section> sections = document.extractSections(false);

            // ->   inner sections
            sections.forEach(section -> annotatedPaper.addSection(section.getName().toLowerCase(),_join(section.getSentences())
            ));

            // ->   abstract
            annotatedPaper.addSection("abstract",_join(document.extractSentences(SentenceSelectorENUM
                    .ONLY_ABSTRACT)));

            // Rhetorical Classes
            List<Sentence> sentences = document.extractSentences(SentenceSelectorENUM.ALL);

            // -> approach
            annotatedPaper.addRhetoricalClass("approach",_join(sentences.stream().filter(s -> s.getRhetoricalClass
                    ().equals(RhetoricalClassENUM.DRI_Approach)).collect(Collectors.toList())));

            // -> background
            annotatedPaper.addRhetoricalClass("background",_join(sentences.stream().filter(s -> s.getRhetoricalClass
                    ().equals(RhetoricalClassENUM.DRI_Background)).collect(Collectors.toList())));

            // -> outcome
            annotatedPaper.addRhetoricalClass("outcome",_join(sentences.stream().filter(s -> s.getRhetoricalClass
                    ().equals(RhetoricalClassENUM.DRI_Outcome)).collect(Collectors.toList())));

            // -> futureWork
            annotatedPaper.addRhetoricalClass("futureWork",_join(sentences.stream().filter(s -> s.getRhetoricalClass
                    ().equals(RhetoricalClassENUM.DRI_FutureWork)).collect(Collectors.toList())));

            // -> challenge
            annotatedPaper.addRhetoricalClass("challenge",_join(sentences.stream().filter(s -> s.getRhetoricalClass
                    ().equals(RhetoricalClassENUM.DRI_Challenge)).collect(Collectors.toList())));


            // Terms
            annotatedPaper.setTerms(document.extractTerminology().stream().map(t -> t.getText()).collect(Collectors
                    .joining
                    (" ")));

            // Summary
            annotatedPaper.setSummary(_join(document.extractSummary(10, SummaryTypeENUM.CENTROID_SECT)));

            // CleanUp
            document.cleanUp();

            Instant end = Instant.now();
            LOG.info("Annotated Document composed successfully from a UPF-Gate document '"+document+"' in: " + ChronoUnit
                    .MINUTES.between(start,end) + "min " + ChronoUnit.SECONDS
                    .between(start,end) + "secs");

        } catch (Exception e) {
            throw new RuntimeException("Error processing document by UPF Text Mining librairy",e);
        }
        return annotatedPaper;
    }


    private String _join(List<Sentence> sentences){
        return sentences.stream().map(s -> s.getText()).collect(Collectors.joining(" "));
    }



}
