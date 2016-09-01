FROM tomcat:8-jre8
MAINTAINER Librairy <librairy.framework@gmail.com>

ENV LIBRAIRY_HOME /librairy
RUN mkdir $LIBRAIRY_HOME

RUN mkdir $LIBRAIRY_HOME/files
RUN mkdir $LIBRAIRY_HOME/files/default
RUN mkdir $LIBRAIRY_HOME/files/custom
RUN mkdir $LIBRAIRY_HOME/files/collected

WORKDIR /librairy

#ENV UPF_URL http://backingdata.org/dri/library/1.3/DRIresources-1.3.tar.gz

ADD http://backingdata.org/dri/library/2.0/DRIresources-2.0.tar.gz /librairy/driresources.tar.gz
#ADD DRIresources-2.0.tar.gz /librairy/driresources.tar.gz

RUN set -x \
#	&& curl -fSL "$UPF_URL" -o driresources.tar.gz \
	&& tar -zxvf driresources.tar.gz \
	&& rm driresources.tar.gz \
	&& chmod -R 777 /librairy/DRIresources-2.0

WORKDIR /usr/local/tomcat

ADD harvester.war /usr/local/tomcat/webapps/harvester.war

ADD waitFor.sh /
RUN chmod +x /waitFor.sh

ENV JAVA_OPTS='-Xmx16g'

VOLUME /librairy
VOLUME /usr/local/tomcat

EXPOSE 8080
CMD ["catalina.sh", "run"]