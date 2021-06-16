# ShotPut
## What is this?
This library serves as a way to quickly and easily document [Javalin](https://javalin.io) endpoints.
## How to use
The system is distributed as a .jar file that you can download on the Github releases page or generate from this source code.
`java -jar shotput.jar {command-line parameters}`
```
usage: javalin-doc
 -?,--help               Print the Help Menu
 -cp,--classpath <arg>   The files to parse (Required)
 -h,--html               Flag to generate html file. If this flag and -h
                         are not set both will be generated
 -o,--outdir <arg>       The place to put the generated files (Required)
 -c,--config <arg>       The config file to use. Otherwise the default will be used.
 -x,--excel              Flag to generate excel file. If this flag and -h
                         are not set both will be generated
```
Unlike Javadoc, we will not create a docs folder for you. You have to specify an existing folder where you would like the documetation stored.
Also the classpath is the folder containing the source code you would like to document. You can only supply one folder at time.

It is also possible to add ShotPut as a maven/gradle dependency. It is available directly from maven central through:
```POM
<dependency>
    <groupId>io.github.bralax</groupId>
    <artifactId>shotput</artifactId>
    <version>0.2</version>
</dependency>
```
There also exists a [Gradle Plugin](https://github.com/bralax/ShotPut-gradle) and a [Maven Plugin](https://github.com/bralax/ShotPut-maven).

### Running the Documentation engine Programmatically
The core class of the Documentation Engine is  `io.github.bralax.shotput.Shotput`. To generate documentation, you need to create an instance of that class and then call it's start method.
The JavalinDoc constructor takes in the following parameters:
* Config config -> An `io.github.bralax.shotput.Config` object with the settings for generating documentation
* File src -> The folder that contains java files or the java file to parse
* boolean excel -> Whether to produce csv documentation
* boolean html -> Whether to produce html documentation
* File out -> The folder to place the documentation in

Running the system programatically has one extra feature. The JavalinDoc object has one extra method, `registerGenerator`. It can be called to register a sample code generator for a specific language. A sample code generator should extend
`io.github.bralax.shotput.code.SampleCodeGenerator` and implement these method:
* `public String getType()` -> Should return the language this generator supports
* `public String generate(String baseUrl, Endpoint endpoint)` -> should create a markdown code block that will create a request for this endpoint.


### The Config File
The system supports using a config file for storing important configs for when generating html. See the provided sample: `exampleConfig.yml`. 

## Documenting Code
The system follows a similar set of rules to a traditional javadoc comment. The javadoc comment should be located directly above a call to create a javalin endpoint. The first line of the javadoc is the title of the endpoint. All text after the first line until the tags is the description. The The one major difference is that this system uses a seperate set of `@tags` from Javadocs. Currently, **None** of the normal javadoc `@tags` are available in JavalinDoc. The available tags for JavalinDoc are:
* `@endpoint` - The address of this endpoint. See point 2 under Limitations to see when this is required
* `@type` - The type of endpoint this is. The type should be in all-caps. This parameter is currently unused but in the future will used when specifying an endpoint outside of Limitation 1. 
* `@authenticated` - A tag indicating that you need to be logged in to access this endpoint. Has no parameters.
* `@group {group}` - The group that this endpoint belongs to. Will be used when organizing the HTML documentation. Defaults to a value of `Endpoint`
* `@queryParam` - A query parameter that the system requests. Should be in the format `@queryParam {parameter} {type} [Required] {description}`
* `@pathParam` - A path parameter that the system requests. Currently all path parameters are required. Should be in the format `@pathParam {parameter} {type} {description}`
* `@formParam` - A form parameter that the system requests. Form parameters have the ability to be "leveled". See section below on Objects and Arrays for how this works. Should be in the format `@formParam {parameter} {type} [Required] {description}`
* `@requestHeader` - A request header that the system requests. Should be in the format `@requestHeader {parameter} {type} [Required] {description}`
* `@responseHeader` - A response header that the system returns. Should be in the format `@responseHeader {parameter} {type} [Required] {description}`
* `@response` - A example response for the endpoint. Should be of the form: `@response {statusCode} ["{reason}"] {example response}`
* `@responseField` - A field found in a response to be used when return type is json. `@responseField {parameter} {type} [Required] {description}`
* `@responseType` - The format of data the system returns (json, text, html).

The parameter type can be one of following:
* `String`
* `Boolean`
* `File`
* `Float`
* `Int`
* `Object`
* `Array`
Note: All of the above tags are block tags.

### Objects and Arrays
Sometimes you want to describe complex json objects/arrays in your formParams. For example, if you want the user to pass to pass an instance of this class to your api:
```JAVA 
public class Rectangle {
    int width;
    int height;
}
```
To do this you need to describe the base form param that needs a rectangle as well as the fields of the rectangle. To do this we allow for `.` notation. So for this example we can write the following:
```JAVA
    /** 
     * ...
     * @formParam rectangle Object Required A rectangle to do something with
     * @formParam rectangle.width Int Required The width of the rectangle
     * @formParam rectangle.height Int Required The height of the rectangle
     * ...
     * /
```

### Example
```JAVA
    /** Example Endpoint
     *  This endpoint does some stuff.
     *  @endpoint /:age
     *  @type GET
     *  @queryParam name String your name for the system to interpret
     *  @pathParam age Int Required your age for the system to think about
     *  @formParam height Int Required your height to be contemplated
     *  @requestHeader token String Required a java web token
     *  @responseHeader cookies String a updated java web token for the future
     *  @responseStatus 200 the system liked you
     *  @responseStatus 403 you are unworthy of access
     *  @responseType json
    */
```

## Limitations
The system is currently limited in what it can interpret. 
1. The system can only find documentation in two locations:
   1. A declaration of a method that takes in 1 parameter (A Javalin Context) (`public void endpoint(Context ctx)`)
   2. Any call to declaring a endpoint on the Javalin object (`javalin.get(...)` etc.)
      * The system can only interpret explicity defined endpoints
            What this means is that the system will only notice when you directly call a method on the javalin object (currently).
            To give an example:
                ```java
                public class Example {

                    public static void main(String[] args) {
                        ...
                        /** 1
                        * ...
                        */
                        get("some endpoint");
                        ...
                    }

                    public static void get(String endpoint) {
                        ...2
                        javalin.get(endpoint, ctx ->{...});
                    }
                }

                ```
                This will notice the call to `javalin.get()` within the get method but it will ignore the call to the wrapper function even if the helper function call in main has a properly structed comment. This means that putting the doc comment at location 1 will not be interpreted but if you put the comment at location 2 the comment will be interpretted. This can cause problems if you wrap access to javalin in helper methods.

2. The system can interpret additional information in specific circumstances.
The system is designed to do some of the work for you but only if you structure your code in a certain way. These "rules"
are not best practices but rather there due to the limitations of the system. It's best practice not to write your code based on these rules but rather take then into account when looking at what gets automatically recognized by the system. The system uses an Abstract Syntax Tree (AST) so it can only determine things that can be found out from the source code prior to running. If you do not follow these rules it just requires more information to be documented by hand.
    1. If the endpoint name is a string constant, the system will interpret it for you and you will not have to use an `@endpoint` tag. 
        For example:
        ```java
            javalin.get("/hello", ctx -> {...});
        ```
        the system will be able to pull out the `/hello` as the endpoint, while in this example:
        ```java
            String endpoint = "/hello";
            javalin.get(endpoint, ctx -> {...});
        ```
        The endpoint will have to be manually specified as internally the system can not determine the runtime value of this variable.
    
    2. If you document on the endpoint registration and the handler is a lambda function `ctx -> {...}` or you document on a method declaration the system will run through the function and try to pull out important pieces of information. It will look for every call to `.queryParam`, `.formParam`,`.pathParam`,`.status` and more to build out a skeleton of the information that needs to be provided in the javadoc comment. Currently, if you write the comment on the endpoint registration, this process of interpretting the content of an endpoint can only be done on a lambda expression. This is due to a current system limitation in which it can not currently locate a function outside the scope of the current function. 


## Why?
Javalin has a core maintained plugin for generating OpenApi schemas which could easily converted into documentation.
That plugin has a few issues that made it hard for me to use in my project. In particular, the documenting mechanism is strange. It use a complex annotation system which could be hard to read for those who have not learned it. I am working on a shared code base where i am the main proponent for documenting. This meant that having a more friendly format (like javadoc) comments were able to lower the barier to entry for the team.


## Credits
* This library makes use of the [Java Parser](http://javaparser.org) AST Generator
* The system internally makes use of Java port of [Scribe](https://github.com/knuckleswtf/scribe) and [Pastel](https://github.com/knuckleswtf/pastel) a fantastic set of documentation tools for PHP and Javascript that is itself related to [Slate](https://github.com/slatedocs/slate).  