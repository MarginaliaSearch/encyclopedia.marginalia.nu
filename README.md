# [encyclopedia.marginalia.nu](https://encyclopedia.marginalia.nu/)

This is a desktop-first low-distraction wikipedia mirror. 
The code isn't super actively maintained and won't win any beauty 
pageants, but it's an interesting project I'm sharing
the sources for posterity.

The system requires JDK20. 

## Build

To build the code, run the following command

```shell
$ ./gradlew assemble distTar
```

The output will be `build/distributions/encyclopedia-1.0-SNAPSHOT.tar`.  
Unpack it somewhere. 

## Initial set-up

To create an articles database, download an appropriate [Wikipedia Zim File](https://dumps.wikimedia.org/other/kiwix/zim/wikipedia/), 
`en_all_nopic` is a good choice for production, `en_100` for testing. 

Before serving the data, the zim file needs to be converted into a SQLite database. 
Convert the file to an articles database `articles.db` by running the command below

```shell
$ encyclopedia-1.0-SNAPSHOT/bin/encyclopedia convert file.zim articles.db
```

This will take a few hours if it's the full Wikipedia dataset.

## Run

To serve the articles, run

```shell
$ encyclopedia-1.0-SNAPSHOT/bin/encyclopedia serve 8080 articles.db
```

Change `8080` to the port you want to serve the traffic from.  At
start-up the server constructs a trie used in searching, this will take
a few minutes.

