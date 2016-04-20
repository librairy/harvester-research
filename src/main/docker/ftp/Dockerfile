FROM stilliard/pure-ftpd
MAINTAINER Librairy <librairy.framework@gmail.com>

VOLUME /home/ftpusers/librairy
RUN apt-get update
RUN apt-get --fix-missing install -y expect
ADD create_user.exp /

RUN expect -f /create_user.exp
RUN pure-pw mkdb
CMD ["/usr/sbin/pure-ftpd", "-c", "50", "-C", "10", "-U", "000:000", "-l", "puredb:/etc/pure-ftpd/pureftpd.pdb"]