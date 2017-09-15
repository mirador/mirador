# Mirador

Mirador is a tool to identify new hypothesis in complex datasets through visual exploration.

## IntelliJ project

Use to edit and debug the code. Already set to run the main class mirador.app.Mirador

## Maven build

Need [Apache Maven](http://maven.apache.org/) installed

```
mvn install assembly:assembly
```

## Create Windows, Linux, and Mac apps

Uses [Packr](https://github.com/libgdx/packr)

* Mac:

```
cd dist
java -jar packr.jar config/macos.json 
```

* Windows:

```
cd dist
java -jar packr.jar config/windows64.json 
```

* Linux:

```
cd dist
java -jar packr.jar config/linux64.json 
```

## Create Windows, Linux, and Mac installers

Run the ant build script inside the dist folder, it will create the corresponding
installation package for each platform.