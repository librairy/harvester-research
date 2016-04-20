# Harvester [![Release Status](https://travis-ci.org/librairy/harvester-file.svg?branch=master)](https://travis-ci.org/librairy/harvester-file) [![Dev Status](https://travis-ci.org/librairy/harvester-file.svg?branch=develop)](https://travis-ci.org/librairy/harvester-file) [![Doc](https://raw.githubusercontent.com/librairy/resources/master/figures/interface.png)](https://rawgit.com/librairy/harvester-file/doc/report/index.html)

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
    - ./data:/librairy/files/uploaded
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
    $ docker run -it --rm --name ftp -p 5051:21 -v /Users/cbadenes/Downloads/ftp:/librairy/files/uploaded librairy/ftp:1.0
    ```

- **Harvester**:
    ```sh
    $ docker run -it --rm --name harvester -v /Users/cbadenes/Downloads/ftp:/librairy/files/uploaded librairy/harvester
    ```

Remember that by using the flags: `-it --rm`, the services runs in foreground mode. Instead, you can deploy it in background mode as a domain service by using: `-d --restart=always`