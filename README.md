## Mirador

Mirador is a tool to identify new hypothesis in complex datasets through visual exploration.

## Maven build

Need [Apache Maven](http://maven.apache.org/) installed

```
mvn install assembly:assembly
```

## Create Windows, Linux, and Mac apps

Uses [Packr](https://github.com/libgdx/packr)

```
cd dist
java -jar packr.jar mac-config.json 
```

```
cd dist
java -jar packr.jar windows64-config.json 
```