# Contributing

This document describes the format and structure of this project in the case that some either ones to help with it or wants to build the repository for themself.

## Building project locally

To generate the .jar file run: 

`mvn assembly:assembly -DdescriptorId=jar-with-dependencies -f "{location of pom.xml file}`

To run the program without creating a .jar file, run:
`mvn exec:java -Dexec.mainClass="JavalinDoc" -Dexec.args="{any command line arguments}"`

## Project overview

### Package overview

The following ASCII file structure diagram shows the basic structure of the repository highlighting important files:

```
org.bralax
├── test/                       // A place containing old code used for testing the system
│   └── Example.java            // An example file to test on
├── html/                       // Everything related to storing and generating html
│   ├── StringContent.java      // Representing a piece of text in the html
│   ├── HtmlAttr.java           // Available attributes that you can assign to an html element
│   ├── HtmlStyle.java          // A file containing an enum of availble class names. **Needs to be cleaned up**
│   ├── TagName.java            // A file containing an enum of tag types from html.
│   └── HtmlTree.java           // A class representing a section of the html document.
├── Endpoint.java               // A small class representing an individual parsed endpoint. Any additional stored information will go here.
├── HTMLGenerator.java          // The code that generates an HTML document from the given endpoints
└── JavalinDoc.java             // The main file of the system currently responsible for command-line, parsing the files and generating the .csv
```

# Future Plans
Here are some of the major goals I have for the project:
* A complete css file that can be used
* The ability to work with the major javalin plugins including:
  * Javalin-graphql
  * Javalin-openapi
  * Javalin-vue
* Better support for sse and websockets. Right now they get the same options as a regular get/post endpoint which probably does not make the most sense.
* The ability to define an endpoint in any javadoc comment
* The ability to parse a handler when the handler is not a lambda function
* A place to put general information about the endpoints below without conflicting with general javadocs
* Logging system 
* Refactor the system to seperate out some of the work from JavalinDoc.java and offer more instantiable classes so that features of the system can be used elsewhere such as the parser or the html generation system.
