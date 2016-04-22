FROM tomcat:8-jre8
MAINTAINER Librairy <librairy.framework@gmail.com>

ENV LIBRAIRY_HOME /librairy
RUN mkdir $LIBRAIRY_HOME

RUN mkdir $LIBRAIRY_HOME/files
RUN mkdir $LIBRAIRY_HOME/files/default
RUN mkdir $LIBRAIRY_HOME/files/uploaded
RUN mkdir $LIBRAIRY_HOME/files/collected

WORKDIR /librairy

#ENV UPF_URL http://backingdata.org/dri/library/1.3/DRIresources-1.3.tar.gz

ADD http://backingdata.org/dri/library/1.2/DRIresources-1.2.tar.gz /librairy/driresources.tar.gz

RUN set -x \
#	&& curl -fSL "$UPF_URL" -o driresources.tar.gz \
	&& tar -zxvf driresources.tar.gz \
	&& rm driresources.tar.gz \
	&& chmod -R 777 /librairy/DRIresources-1.2

WORKDIR /usr/local/tomcat

ADD harvester.war /usr/local/tomcat/webapps/harvester.war

ENV COLUMN_DB_HOST column-db
ENV COLUMN_DB_PORT 9042

ENV DOCUMENT_DB_HOST document-db
ENV DOCUMENT_DB_PORT 9300

ENV GRAPH_DB_HOST graph-db
ENV GRAPH_DB_PORT 7474

ENV MESSAGE_BROKER_HOST event-bus
ENV MESSAGE_BROKER_PORT 5672

ENV JAVA_OPTS='-Xmx16g'

VOLUME /librairy
VOLUME /usr/local/tomcat

EXPOSE 8080
CMD ["catalina.sh", "run"]