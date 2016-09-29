/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research.data;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 22/04/16:
 *
 * @author cbadenes
 */
@Data
@ToString
public class AnnotatedPaper implements Serializable{

    private static final Logger LOG = LoggerFactory.getLogger(AnnotatedPaper.class);

    String doi      = "";

    String title    = "";

    String authors  = "";

    String year     = "";

    String tokens   = "";

    String terms    = "";

    String content  = "";

    String summary  = "";

    Map<String,String> sections = new HashMap();

    Map<String,String> rhetoricalClasses = new HashMap();


    public AnnotatedPaper addSection(String name, String content){
        sections.put(name,content);
        return this;
    }

    public AnnotatedPaper addRhetoricalClass(String name, String content){
        rhetoricalClasses.put(name, content);
        return this;
    }

    public String toString(){
        return new StringBuilder().append("[Paper: ").append(this.getTitle()).append(" ]").toString();
    }

}
