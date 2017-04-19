/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research.descriptor;

import com.google.common.io.Files;
import org.librairy.harvester.file.descriptor.Descriptor;
import org.librairy.harvester.file.descriptor.FileDescription;
import org.librairy.harvester.research.data.AnnotatedPaper;
import org.librairy.harvester.research.processor.DocumentProcessor;
import org.librairy.model.domain.resources.MetaInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

/**
 * Created on 20/04/16:
 *
 * @author cbadenes
 */
//@Component("ResearchDescriptor")
public class ResearchDescriptor implements Descriptor{

    private static final Logger LOG = LoggerFactory.getLogger(ResearchDescriptor.class);

    @Autowired
    DocumentProcessor processor;


    Set<String> processed = new TreeSet<>();

    @Override
    public FileDescription describe(File file) {
        LOG.info("Trying to get description from file: " + file.getAbsolutePath());
        FileDescription fileDescription = new FileDescription();
        if (processed.contains(file.getAbsolutePath())){
            LOG.error("File already processed!!!!");
            return fileDescription;

        }
        processed.add(file.getAbsolutePath());
        try {


            AnnotatedPaper document = processor.process(file.getAbsolutePath());

            // Summary
            fileDescription.setSummary(document.getSummary());

            // MetaInformation
            MetaInformation metaInformation = new MetaInformation();
            metaInformation.setFormat(Files.getFileExtension(file.getAbsolutePath()));
            metaInformation.setTitle(document.getTitle());
            metaInformation.setType("research-paper");
            metaInformation.setDescription(document.getSummary());
            metaInformation.setPublished(document.getYear());
            metaInformation.setAuthored(document.getAuthors());
            //metaInformation.setContributors();
            metaInformation.setCreators(document.getAuthors());
            metaInformation.setLanguage("en");
            //metaInformation.setRights();
            fileDescription.setMetaInformation(metaInformation);

        } catch (ExecutionException e) {
            LOG.warn("Error getting description from file: " + file.getAbsolutePath(),e);
        }
        LOG.info("Description completed for: " + file.getAbsolutePath());
        return fileDescription;
    }
}
