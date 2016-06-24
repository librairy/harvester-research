# Harvester 
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b57869f0368743969edea71d4692303e)](https://www.codacy.com/app/cbadenes/harvester-research)
[![Release Status](https://travis-ci.org/librairy/harvester-research.svg?branch=master)](https://travis-ci.org/librairy/harvester-research)
[![Dev Status](https://travis-ci.org/librairy/harvester-research.svg?branch=master)](https://travis-ci.org/librairy/harvester-research)
[![Coverage Status](https://coveralls.io/repos/github/librairy/harvester-research/badge.svg?branch=master)](https://coveralls.io/github/librairy/harvester-research?branch=master)
[![Doc](https://raw.githubusercontent.com/librairy/resources/master/figures/interface.png)](https://rawgit.com/librairy/harvester-research/doc/report/index.html)


Collect and process unstructured files to retrieve the full-text content and derived tokens from them.

## Get Started!

A prerequisite to consider is to have installed [Docker-Compose](https://docs.docker.com/compose/) in your system.

You can run this service in a isolated way (see *Distibuted Deployment* section) or as extension of the [explorer](https://github.com/librairy/explorer).
In that case, add the following services to the existing `docker-compose.yml` file:

```yml
ftp:
  container_name: ftp
  image: librairy/ftp:1.0
  ports:
    - "5051:21"
  volumes:
    - ./data:/home/ftpusers/librairy
harvester:
  container_name: harvester
  image: librairy/harvester
  volumes:
    - ./data:/librairy/files/custom
  links:
      - column-db
      - document-db
      - graph-db
      - event-bus
```

and then, deploy it by typing:

```sh
$ docker-compose up
```
That's all!! **librairy harvester** should be run in your system now along with **librairy explorer**.

## Distributed Deployment

Instead of deploy all containers as a whole, you can deploy each of them independently. It is useful to run the service in a distributed way deployed in several host-machines.

- **FTP Server**:
    ```sh
    $ docker run -it --rm --name ftp -p 5051:21 -v ./ftp:/librairy/files/custom librairy/ftp:1.0
    ```

- **Harvester**:
    ```sh
    $ docker run -it --rm --name harvester -v ./documents:/librairy/files librairy/harvester
    ```

Remember that by using the flags: `-it --rm`, the services runs in foreground mode. Instead, you can deploy it in background mode as a domain service by using: `-d --restart=always`