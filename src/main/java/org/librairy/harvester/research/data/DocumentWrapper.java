package org.librairy.harvester.research.data;

import edu.upf.taln.dri.lib.exception.DRIexception;
import edu.upf.taln.dri.lib.exception.InternalProcessingException;
import edu.upf.taln.dri.lib.model.Document;
import edu.upf.taln.dri.lib.model.DocumentImpl;
import edu.upf.taln.dri.lib.model.ext.RhetoricalClassENUM;
import edu.upf.taln.dri.lib.model.ext.Sentence;
import edu.upf.taln.dri.lib.model.ext.SentenceSelectorENUM;
import edu.upf.taln.dri.lib.model.ext.SummaryTypeENUM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by cbadenes on 07/01/16.
 */
public class DocumentWrapper implements Serializable{

    private static final Logger LOG = LoggerFactory.getLogger(DocumentWrapper.class);

    public static final String ABSTRACT_SECTION= "Abstract";

    private transient DocumentImpl document;
    private List<Sentence> sentences;
    private String name;
//    private final SectionReader sectionReader;

    public DocumentWrapper(){};

    public DocumentWrapper(Document document)  {
        loadFrom(document);
    }

    public void loadFrom(Document document){
        try{
            // Name of Document
            this.name               = document.getName();

            // Raw document
            this.document           = (DocumentImpl) document;

            // Sentences
            this.sentences          = document.extractSentences(SentenceSelectorENUM.ALL);

//            this.sectionReader      = new SectionReader(document.extractSections(false));

        }catch (DRIexception e){
            throw new RuntimeException(e);
        }
    }

    public gate.Document getGateDocument(){
        return document.cacheManager.getGateDoc();
    }

    public String getContent() {
        try {
            return document.getRawText();
        } catch (InternalProcessingException e) {
            LOG.error("Error getting raw text from: " + document, e);
            throw new RuntimeException(e);
        }
    }

    public String getXml() {
        try {
            return document.getXMLString();
        } catch (InternalProcessingException e) {
            LOG.error("Error getting XML text from: " + document, e);
            throw new RuntimeException(e);
        }
    }


    public String getTitle(){
        try{
            return this.document.extractHeader().getTitle().trim();
        } catch (InternalProcessingException e) {
            LOG.error("Error getting title from: " + document, e);
            throw new RuntimeException(e);
        }
    }

    public String getDoi(){
        try{
            return this.document.extractHeader().getDoi();
        } catch (InternalProcessingException e) {
            LOG.error("Error getting doi from: " + document, e);
            throw new RuntimeException(e);
        }
    }

    public String getYear(){
        try{
            return this.document.extractHeader().getYear();
        } catch (InternalProcessingException e) {
            LOG.error("Error getting year from: " + document, e);
            throw new RuntimeException(e);
        }
    }

    public List<AuthorWrapper> getAuthors(){
        try{
            return  this.document.extractHeader().getAuthorList().stream().map(AuthorWrapper::new).collect(Collectors.toList());
        } catch (InternalProcessingException e) {
            LOG.error("Error getting authors from: " + document, e);
            throw new RuntimeException(e);
        }
    }

    public List<String> listSections() {
        try{
            List<String> sections = document.extractSections(false).stream().map(s -> s.getName()).collect(Collectors.toList());
            sections.add(ABSTRACT_SECTION);
            return sections;
        } catch (DRIexception e) {
            LOG.error("Error getting sections from: " + document, e);
            throw new RuntimeException(e);
        }
    }


    public String getAbstractContent(){
        try{
            return join(this.document.extractSentences(SentenceSelectorENUM.ONLY_ABSTRACT).stream());
        } catch (InternalProcessingException e) {
            LOG.error("Error getting name from: " + document, e);
            throw new RuntimeException(e);
        }
    }

    public String getApproachContent(){
        return join(this.sentences.stream().filter(sentence -> sentence.getRhetoricalClass().equals(RhetoricalClassENUM.DRI_Approach)));
    }

    public String getBackgroundContent(){
        return join(this.sentences.stream().filter(sentence -> sentence.getRhetoricalClass().equals(RhetoricalClassENUM.DRI_Background)));
    }

    public String getChallengeContent(){
        return join(this.sentences.stream().filter(sentence -> sentence.getRhetoricalClass().equals(RhetoricalClassENUM.DRI_Challenge)));
    }

    public String getFutureWorkContent(){
        return join(this.sentences.stream().filter(sentence -> sentence.getRhetoricalClass().equals(RhetoricalClassENUM.DRI_FutureWork)));
    }

    public String getOutcomeContent(){
        return join(this.sentences.stream().filter(sentence -> sentence.getRhetoricalClass().equals(RhetoricalClassENUM.DRI_Outcome)));
    }

    public String getSummaryByCentroidContent(int lines){
        try{
            return join(document.extractSummary(lines, SummaryTypeENUM.CENTROID_SECT).stream());
        } catch (InternalProcessingException e) {
            LOG.error("Error getting summary by centroid from: " + document, e);
            throw new RuntimeException(e);
        }
    }

    public String getSummaryByTitleSimContent(int lines){
        try{
            return join(document.extractSummary(lines, SummaryTypeENUM.TITILE_SIM).stream());
        } catch (InternalProcessingException e) {
            LOG.error("Error getting summary by title from: " + document, e);
            throw new RuntimeException(e);
        }
    }


//    public String getContentOfSection(String section){
//        LOG.debug("reading section: '"+section+"' from: " + name);
//        try{
//            if (section.equalsIgnoreCase(ABSTRACT_SECTION)) return getAbstractContent();
//            return join(document.extractSections(false).stream().filter(s -> sectionReader.isContainedIn(s, section)).flatMap(s -> s.getSentences().stream()));
//        } catch (DRIexception  e) {
//            LOG.error("Error getting text from: " + document, e);
//            throw new RuntimeException(e);
//        }
//    }

    private String join(Stream<Sentence> sentences){
        return sentences.map(s -> s.getText()).collect(Collectors.joining(" "));
    }

    public void clean(){
        try {
            this.document.cleanUp();
        } catch (Exception e) {
            LOG.warn("Error when cleaning document: " + document, e);
        }
    }

    public static void serialize(DocumentWrapper document, String path){
        try{
            LOG.debug("Serializing  in:  " + path);
            File file = new File(path);
            gate.Document gateDocument = document.getGateDocument();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(gateDocument);
            oos.close();
            LOG.info("Serialized : " + document + " in:  " + path);
        }catch(Exception ex){
            LOG.error("Error serializing annotated document: " + document + " to file: " + path);
        }
    }

    public static DocumentWrapper deserialize(String path){
        try{
            LOG.info("Deserializing file:  " + path);
            File file = new File(path);
            FileInputStream fin = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fin);
            Object object = ois.readObject();
            gate.Document gateDocument = (gate.Document) object;
            DocumentWrapper annotatedDocument = new DocumentWrapper(new DocumentImpl(gateDocument));
            LOG.info("Deserialized:  " + path);
            return annotatedDocument;
        }catch(Exception e){
            throw new RuntimeException("Error deserializing document: " + path, e);
        }
    }
}
