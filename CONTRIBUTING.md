# Contributing

This document describes the format and structure of this project in the case that some either ones to help with it or wants to build the repository for themself.

## Building project locally

To generate the .jar file run: 

`mvn clean compile assembly:single -Pjar`

The jar will be located at:
`target/javalindoc-{version}-jar-with-dependencies.jar`

To Deploy the tool to maven central:
`GPG_TTY=$(tty) MAVEN_OPTS="--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED"  mvn clean deploy -Possrh`

To run the program without creating a .jar file, run:
`mvn exec:java -Dexec.mainClass="JavalinDoc" -Dexec.args="{any command line arguments}"`

## Project overview

### Package overview

The following ASCII file structure diagram shows the basic structure of the repository highlighting important files:

```
io.github.bralax.shotput
├── CLI.java                      // All code relating to using the system from the command line
├── Config.java                   // An object representing the configuration settings
├── ConfigParser.java             // A class for parsing a config .yml file into a config object
├── Shotput.java                  // The core class for the whole system
├── code                          // All code relating to generating sample code to use a endpoint
│   ├── BashGenerator.java        // System to generate Bash curl requests, could use more work
│   ├── JavaGenerator.java        // System to generate Java unires requests
│   └── SampleCodeGenerator.java  // Base class for code generation. Provides helper methods for generating urls and parameters
├── endpoint                      // Folder containg object representing the intepretted endpoints
│   ├── BodyParameter.java        // A layered version of a parameter allowing them to be namespaced to handle objects/arrays
│   ├── Endpoint.java             // A small class representing an individual parsed endpoint.
│   ├── Parameter.java            // A class representing an individual parameter parsed from the code
│   └── Response.java             // A class representing an individual response parsed from the code
├── html                          // All code responsible for takking the markdown code and converting it to html
│   ├── Pastel.java               // Main code to convert the markdown to html
│   └── PastelUtil.java           // Helper methods used by the html velocity templates
├── markdown                      // All code related to parsing and creating markdown code
│   ├── CopyVisitor.java          // Code used by Pastel.java to parse the markdown file
│   ├── MarkdownWriterUtils.java  // Helper methods used by the markdown velocity templates
│   └── Scribe.java               // The main code for converting the endpoints into markdown
├── openapi                       // All code related to generating openapi specs
│   └── OpenApiGenerator.java     // The code for generating the spec. Needs more work
└── parser                        // Folder that contains all code parsing
    ├── CodeParser.java           // Main class that parses all the code
    ├── JavadocParser.java        // Methods used to interpret and parse a javadoc comment
    ├── MethodParser.java         // Methods used to parse the body of a method
    └── ParserHelpers.java        // Extra helper methods used throughout the code parses.
```

# Future Plans
Here are some of the major goals I have for the project:
* The ability to work with the major javalin plugins including:
  * Javalin-graphql
  * Javalin-openapi
  * Javalin-vue
* Better support for sse and websockets. Right now they get the same options as a regular get/post endpoint which probably does not make the most sense.
* The ability to parse a handler when the handler is not a lambda function
