/*
 * Copyright (c) 2017. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research;

import lombok.Data;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@Data
public class TermItem {

    private String term = "";

    private Double cvalue;

    private Double consensus;

    private Double pertinence;

    private Double termhood;

    private Double probability;
}
