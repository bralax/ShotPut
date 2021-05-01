# Javalin Doc
## What is this?
This library serves as a way to quickly and easily document [Javalin](https://javalin.io) endpoints.
## How to use
The system is distributed as a .jar file that you can download on the Github releases page or generate from this source code.
`java -jar javalinDoc.jar {command-line parameters}`
```
usage: javalin-doc
 -?,--help               Print the Help Menu
 -cp,--classpath <arg>   The files to parse (Required)
 -h,--html               Flag to generate html file. If this flag and -h
                         are not set both will be generated
 -o,--outdir <arg>       The place to put the generated files (Required)
 -s,--css <arg>          The css file to be used on the html
 -x,--excel              Flag to generate excel file. If this flag and -h
                         are not set both will be generated
```
Unlike Javadoc, we will not create a docs folder for you. You have to specify an existing folder where you would like the documetation stored.
Also the classpath is the folder containing the source code you would like to document. You can only supply one folder at time.

## Documenting Code
The system follows a similar set of rules to a traditional javadoc comment. The javadoc comment should be located directly above a call to create a javalin endpoint. The one major difference is that this system uses a seperate set of `@tags` from Javadoc. Currently, **None** of the normal javadoc `@tags` are available in JavalinDoc. The available tags for JavalinDoc are:
* `@endpoint` - The address of this endpoint. See point 2 under Limitations to see when this is required
* `@type` - The type of endpoint this is. The type should be in all-caps. This parameter is currently unused but in the future will used when specifying an endpoint outside of Limitation 1. 
* `@authenticated` - A tag indicating that you need to be logged in to access this endpoint. Has no parameters.
* `@queryParam` - A query parameter that the system requests. Should be in the format `@queryParam {parameter} {type} [Required] {description}`
* `@pathParam` - A path parameter that the system requests. Currently all path parameters are required. Should be in the format `@pathParam {parameter} {type} {description}`
* `@formParam` - A form parameter that the system requests. Form parameters have the ability to be "leveled". See section below on Objects and Arrays for how this works. Should be in the format `@formParam {parameter} {type} [Required] {description}`
* `@requestHeader` - A request header that the system requests. Should be in the format `@tag {parameter} {type} [Required] {description}`
* `@responseHeader` - A response header that the system returns. Should be in the format `@tag {parameter} {type} [Required] {description}`
* `@exampleResponse` - A example response for the endpoint. Should be of the form: `@tag {description} : {example response}`
* `@responseStatus` - A status code that the system could return. Should be in the format `@tag {code} {reason for code}`
* `@responseType` - The type of data the system returns 
  

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
    /** This is an example endpoint.
     *  This endpoint does some stuff.
     *  @endpoint /{age}/
     *  @type GET
     *  @queryParam name your name for the system to interpret
     *  @pathParam age your age for the system to think about
     *  @formParam height your hieght to be contemplated
     *  @requestHeader token a java web token
     *  @responseHeader cookies a updated java web token for the future
     *  @responseStatus 200 the system liked you
     *  @responseStatus 403 you are unworthy of access
     *  @responseType json
    */
```

## Limitations
The system is currently limited in what it can interpret. 
1. The system can only interpret explicity defined endpoints
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
    This will notice the call to `javalin.get()` within the get method but it will ignore the call to the wrapper function even if the helper function call in main has a properly structed comment. This means that putting the doc comment at location 1 will not be interpreted but if you put the comment at location 2 the comment will be interpretted.


1. The system can interpret additional information in specific circumstances.
The system is designed to do some of the work for you but only if you structure your code in a certain way. These "rules"
are not best practices but rather there due to the limitations of the system. It's best practice not to write your code based on these rules but rather take then into account when looking at what gets automatically recognized by the system. If you do not follow these rules it just requires more information to be documented by hand.
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
    
    2. If the endpoint handler is a lambda function `ctx -> {...}` the system will run through the function and try to pull out important pieces of information. It will look for every call to `.queryParam`, `.formParam`,`.pathParam`,`.status` and more to build out a skeleton of the information that needs to be provided in the javadoc comment. Currently this process of interpretting the content of an endpoint can only be done on a lambda expression. This is due to a current system limitation in which it can not currently locate a function outside the scope of the current function. Also similar to 1, a call to these methods will be ignored if the "field" parameter in the call is a variable not a constant.


## Building

The system is based around maven. To build a jar run: 
`mvn clean compile assembly:single`
The jar will be located at:
`target/javalindoc-{version}-jar-with-dependencies.jar`

## Credits
* This library makes use of the [Java Parser](http://javaparser.org) AST Generator
* The system internally makes use of Java port of [Scribe](https://github.com/knuckleswtf/scribe) and [Pastel](https://github.com/knuckleswtf/pastel) a fantastic set of documentation tools for PHP and Javascript that is itself related to [Slate](https://github.com/slatedocs/slate).  