# Mirador

Mirador is a tool to identify new hypothesis in complex datasets through visual exploration.

## IntelliJ project

Use to edit and debug the code. Already set to run the main class mirador.app.Mirador.

## Maven build

It creates a single jar containing the bytecode of the project and all its dependencies: 

```
mvn install assembly:assembly
```

The resulting mirador.jar will be placed inside the target folder. Mirador can be run directly from this jar.

Needs [Apache Maven](http://maven.apache.org/) installed.

## Create Windows, Linux, and Mac apps

After assembling the single jar with Maven, you can use [Packr](https://github.com/libgdx/packr) to 
generate native apps for each platform.

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

Run the ant build script inside the dist folder (again, after jar assembly with mMven), it will create the corresponding
installation package for each platform.