/*
 * Copyright (c) 2017. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research.siggraph;

import lombok.Data;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Data
public class Paper {

    String abstractContent;

    String doi;

    String paper_id;

    String title;

    String pdfUrl;

    String author;

    String journal;

    String date;

    String volumeNo;

    String issueNo;

    String pdfFile;

    String originFilename;

    String id;

    public void setAbstract(String content){
        this.abstractContent = content;
    }

    public String getAbstract(){
        return this.abstractContent;
    }


    public void setPaper_id(String id){
        this.paper_id = id;
    }

    public String getPaper_id(){
        return this.paper_id;
    }


}
