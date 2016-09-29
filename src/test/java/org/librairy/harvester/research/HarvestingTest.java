/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.harvester.research;

import es.cbadenes.lab.test.IntegrationTest;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.*;
import org.librairy.storage.UDM;
import org.librairy.model.utils.TimeUtils;
import org.librairy.storage.generator.URIGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by cbadenes on 14/01/16.
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
@TestPropertySource(properties = {
        "harvester.input.folder                 = src/test/resources/workspace",
        "harvester.input.folder.default         = src/test/resources/workspace/default",
        "harvester.input.folder.meta            = src/test/resources/workspace/meta",
        "harvester.input.folder.external        = src/test/resources/workspace/custom",
        "harvester.input.folder.hoarder         = src/test/resources/workspace/collected",
        "librairy.cassandra.contactpoints       = wiig.dia.fi.upm.es",
        "librairy.cassandra.port                = 5011",
        "librairy.cassandra.keyspace            = research",
        "librairy.elasticsearch.contactpoints   = wiig.dia.fi.upm.es",
        "librairy.elasticsearch.port            = 5021",
        "librairy.neo4j.contactpoints           = wiig.dia.fi.upm.es",
        "librairy.neo4j.port                    = 5030",
        "librairy.eventbus.host                 = wiig.dia.fi.upm.es",
        "librairy.eventbus.port                 = 5041"
})
public class HarvestingTest {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestingTest.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;
    
//    @Test
    public void run() throws InterruptedException {
        LOG.info("Sleepping...");
        Thread.sleep(12000000);
        LOG.info("Wake Up!");
    }

//    @Test
    public void simulate() throws InterruptedException {

        // Reset DDBDD
        udm.delete(Resource.Type.ANY);

        // Domain
        Domain domain = new Domain();
        domain.setUri("http://librairy.org/domains/1f02ae0b-7d96-42c6-a944-25a3050bf1e2");
        domain.setName("test-domain");
        udm.save(domain);

        // Source
        Source source = new Source();
        source.setUri(uriGenerator.newFor(Resource.Type.SOURCE));
        source.setCreationTime(TimeUtils.asISO());
//        source.setUrl("file://sample");
        source.setUrl("file://sig2006");
        udm.save(source);
        udm.save(Relation.newComposes(source.getUri(),domain.getUri()));

//        LOG.info("sleep..");
//        Thread.sleep(300000);
//        LOG.info("wake up!");
    }

//    @Test
    public void simulateDocuments() throws InterruptedException {

        // Reset DDBDD
        udm.delete(Resource.Type.ANY);

        // Domain
        Domain domain = new Domain();
        domain.setUri("http://librairy.org/domains/1f02ae0b-7d96-42c6-a944-25a3050bf1e2");
        domain.setName("test-domain");
        udm.save(domain);

        // Source
        Source source = new Source();
        source.setUri(uriGenerator.newFor(Resource.Type.SOURCE));
        source.setCreationTime(TimeUtils.asISO());
        source.setUrl("file://sample");
        udm.save(source);
        udm.save(Relation.newComposes(source.getUri(),domain.getUri()));

        // Document 1
        Document document1 = new Document();
        document1.setUri(uriGenerator.newFor(Resource.Type.DOCUMENT));
        document1.setCreationTime(TimeUtils.asISO());
        document1.setTitle("title-1");
        document1.setAuthoredOn("20160112T12:07");
        document1.setAuthoredBy("");
        document1.setContent("After graduating nearly 3,000 pilots, it was disbanded in late 1944, when there was no further need to train Australian aircrews for service in Europe. The school was re-established in 1946 at Uranquinty, New South Wales, and transferred to Point Cook the following year. To cope with the demands of the Korean War and Malayan Emergency, it was re-formed as No. 1 Applied Flying Training School in 1952 and moved to Pearce, Western Australia, in 1958. Another school was meanwhile formed at Uranquinty, No. 1 Basic Flying Training School (No. 1 BFTS), which transferred to Point Cook in 1958. In 1969, No. 1 AFTS was re-formed as No. 2 Flying Training School and No. 1 BFTS was re-formed as No.");
        document1.setTokens("After graduating nearly 3,000 pilots, it was disbanded in late 1944, when there was no further need to train Australian aircrews for service in Europe. The school was re-established in 1946 at Uranquinty, New South Wales, and transferred to Point Cook the following year. To cope with the demands of the Korean War and Malayan Emergency, it was re-formed as No. 1 Applied Flying Training School in 1952 and moved to Pearce, Western Australia, in 1958. Another school was meanwhile formed at Uranquinty, No. 1 Basic Flying Training School (No. 1 BFTS), which transferred to Point Cook in 1958. In 1969, No. 1 AFTS was re-formed as No. 2 Flying Training School and No. 1 BFTS was re-formed as No.");

        udm.save(document1);
        udm.save(Relation.newProvides(source.getUri(),document1.getUri()));
        udm.save(Relation.newContains(domain.getUri(),document1.getUri()));

        // Document 2
        Document document2 = new Document();
        document2.setUri(uriGenerator.newFor(Resource.Type.DOCUMENT));
        document2.setCreationTime(TimeUtils.asISO());
        document2.setTitle("title-2");
        document2.setAuthoredOn("20160112T12:07");
        document2.setAuthoredBy("");
        document2.setContent("The arts is a vast subdivision of culture, composed of many creative endeavors and disciplines. It is a broader term than \"art\", which, as a description of a field, usually means only the visual arts. The arts encompass the visual arts, the literary arts and the performing arts – music, theatre, dance and film, among others. This list is by no means comprehensive, but only meant to introduce the concept of the arts. For all intents and purposes, the history of the arts begins with the history of art. The arts might have origins in early human evolutionary prehistory. Ancient Greek art saw the veneration of the animal form and the development of equivalent skills to show musculature, poise, beauty and anatomically correct proportions. Ancient Roman art depicted gods as idealized humans, shown with characteristic distinguishing features (e.g. Jupiter's thunderbolt). In Byzantine and Gothic art of the Middle Ages, the dominance of the church insisted on the expression of biblical and not material truths. Eastern art has generally worked in a style akin to Western medieval art, namely a concentration on surface patterning and local colour (meaning the plain colour of an object, such as basic red for a red robe, rather than the modulations of that colour brought about by light, shade and reflection). A characteristic of this style is that the local colour is often defined by an outline (a contemporary equivalent is the cartoon). This is evident in, for example, the art of India, Tibet and Japan. Religious Islamic art forbids iconography, and expresses religious ideas through geometry instead. The physical and rational certainties depicted by the 19th-century Enlightenment were shattered not only by new discoveries of relativity by Einstein and of unseen psychology by Freud, but also by unprecedented technological development. Paradoxically the expressions of new technologies were greatly influenced by the ancient tribal arts of Africa and Oceania, through the works of Paul Gauguin and the Post-Impressionists, Pablo Picasso and the Cubists, as well as the Futurists and others.");
        document2.setTokens("The arts is a vast subdivision of culture, composed of many creative endeavors and disciplines. It is a broader term than \"art\", which, as a description of a field, usually means only the visual arts. The arts encompass the visual arts, the literary arts and the performing arts – music, theatre, dance and film, among others. This list is by no means comprehensive, but only meant to introduce the concept of the arts. For all intents and purposes, the history of the arts begins with the history of art. The arts might have origins in early human evolutionary prehistory. Ancient Greek art saw the veneration of the animal form and the development of equivalent skills to show musculature, poise, beauty and anatomically correct proportions. Ancient Roman art depicted gods as idealized humans, shown with characteristic distinguishing features (e.g. Jupiter's thunderbolt). In Byzantine and Gothic art of the Middle Ages, the dominance of the church insisted on the expression of biblical and not material truths. Eastern art has generally worked in a style akin to Western medieval art, namely a concentration on surface patterning and local colour (meaning the plain colour of an object, such as basic red for a red robe, rather than the modulations of that colour brought about by light, shade and reflection). A characteristic of this style is that the local colour is often defined by an outline (a contemporary equivalent is the cartoon). This is evident in, for example, the art of India, Tibet and Japan. Religious Islamic art forbids iconography, and expresses religious ideas through geometry instead. The physical and rational certainties depicted by the 19th-century Enlightenment were shattered not only by new discoveries of relativity by Einstein and of unseen psychology by Freud, but also by unprecedented technological development. Paradoxically the expressions of new technologies were greatly influenced by the ancient tribal arts of Africa and Oceania, through the works of Paul Gauguin and the Post-Impressionists, Pablo Picasso and the Cubists, as well as the Futurists and others.");

        udm.save(document2);
        udm.save(Relation.newProvides(source.getUri(),document2.getUri()));
        udm.save(Relation.newContains(domain.getUri(),document2.getUri()));

        // Document 3
        Document document3 = new Document();
        document3.setUri(uriGenerator.newFor(Resource.Type.DOCUMENT));
        document3.setCreationTime(TimeUtils.asISO());
        document3.setTitle("title-3");
        document3.setAuthoredOn("20160112T12:07");
        document3.setAuthoredBy("");
        document3.setContent("Mysticism is the pursuit of communion with, identity with, or conscious awareness of an ultimate reality, divinity, spiritual truth, or God through direct experience, intuition, instinct or insight. Mysticism usually centers on practices intended to nurture those experiences. Mysticism may be dualistic, maintaining a distinction between the self and the divine, or may be nondualistic. Such pursuit has long been an integral part of the religious life of humanity. Within established religion it has been explicitly expressed within monasticism, where rules governing the everyday life of monks and nuns provide a framework conducive to the cultivation of mystical states of consciousness.");
        document3.setTokens("Mysticism is the pursuit of communion with, identity with, or conscious awareness of an ultimate reality, divinity, spiritual truth, or God through direct experience, intuition, instinct or insight. Mysticism usually centers on practices intended to nurture those experiences. Mysticism may be dualistic, maintaining a distinction between the self and the divine, or may be nondualistic. Such pursuit has long been an integral part of the religious life of humanity. Within established religion it has been explicitly expressed within monasticism, where rules governing the everyday life of monks and nuns provide a framework conducive to the cultivation of mystical states of consciousness.");


        udm.save(document3);
        udm.save(Relation.newProvides(source.getUri(),document3.getUri()));
        udm.save(Relation.newContains(domain.getUri(),document3.getUri()));

    }

}
