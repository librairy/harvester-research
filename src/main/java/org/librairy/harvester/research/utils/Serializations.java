/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created on 22/04/16:
 *
 * @author cbadenes
 */
public class Serializations {

    private static final Logger LOG = LoggerFactory.getLogger(Serializations.class);

    public static void serialize(Object document, String path){
        try{
            LOG.info("Serializing:  " + path);
            File file = new File(path);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(document);
            oos.close();
            LOG.info("Serialized: " + document + " in:  " + path);
        }catch(Exception ex){
            LOG.error("Error serializing annotated document: " + document + " to file: " + path);
        }
    }

    public static <T> T deserialize(Class<T> clazz, String path){
        try{
            LOG.info("Deserializing file:  " + path);
            File file = new File(path);
            FileInputStream fin = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fin);
            Object object = ois.readObject();
            LOG.info("Deserialized:  " + path);
            return clazz.cast(object);
        }catch(Exception e){
            throw new RuntimeException("Error deserializing document: " + path, e);
        }
    }


}
