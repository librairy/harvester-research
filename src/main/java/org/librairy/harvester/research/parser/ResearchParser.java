/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research.parser;

import org.librairy.harvester.file.parser.ParsedDocument;
import org.librairy.harvester.file.parser.Parser;
import org.librairy.harvester.research.data.AnnotatedPaper;
import org.librairy.harvester.research.processor.DocumentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Created on 20/04/16:
 *
 * @author cbadenes
 */
@Component
public class ResearchParser implements Parser{

    private static final Logger LOG = LoggerFactory.getLogger(ResearchParser.class);

    @Autowired
    DocumentProcessor processor;


    @Override
    public ParsedDocument parse(File file) {

        LOG.info("Trying to parse file: " + file.getAbsolutePath());
        ParsedDocument parsedDocument = new ParsedDocument();
        try {
            AnnotatedPaper annotatedPaper = processor.process(file.getAbsolutePath());
            parsedDocument.setText(annotatedPaper.getContent());

        } catch (ExecutionException e) {
            LOG.warn("Error parsing file: " + file.getAbsolutePath(),e);
        }
        LOG.info("Parsed completed for: " + file.getAbsolutePath());
        return parsedDocument;
    }
}
