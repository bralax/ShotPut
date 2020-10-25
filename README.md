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
* `@endpointType` - The type of endpoint this is. The type should be in all-caps. This parameter is currently unused but in the future will used when specifying an endpoint outside of Limitation 1. 
* `@endpointQueryParam` - A query parameter that the system requests. Should be in the format `@tag {parameter} {description}`
* `@endpointPathParam` - A path parameter that the system requests. Should be in the format `@tag {parameter} {description}`
* `@endpointFormParam` - A form parameter that the system requests. Should be in the format `@tag {parameter} {description}`
* `@endpointRequestHeader` - A request header that the system requests. Should be in the format `@tag {header name} {description}`
* `@endpointResponseHeader` - A response header that the system returns. Should be in the format `@tag {header name} {description}`
* `@endpointStatus` - A status code that the system could return. Should be in the format `@tag {code} {reason for code}`
* `@endpointResponseType` - The type of data the system returns 
  
Note: All of the above tags are block tags.

### Example
```java
    /** This is an example endpoint.
     *  This endpoint does some stuff.
     *  @endpoint /{age}/
     *  @endpointType GET
     *  @endpointQueryParam name your name for the system to interpret
     *  @endpointPathParam age your age for the system to think about
     *  @endpointFormParam height your hieght to be contemplated
     *  @endpointRequestHeader token a java web token
     *  @endpointResponseHeader cookies a updated java web token for the future
     *  @endpointStatus 200 the system liked you
     *  @endpointStatus 403 you are unworthy of access
     *  @endpointResponseType json
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
            /**
             * ...
            */
            get("some endpoint");
            ...
        }

        public static void get(String endpoint) {
            ...
            javalin.get(endpoint, ctx ->{...});
        }
    }

    ```
    This will notice the call to `javalin.get()` within the get method but it will ignore the call to the wrapper function even 
    if the helper function call in main has a properly structed comment. This is a current limitation of the system and should become more flexible overtime.


2. The system can interpret additional information in specific circumstances.
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
    
    2. If the endpoint handler is a lambda function `ctx -> {...}` the system will run through the function and try to pull out important pieces of information. It will look for every call to `.queryParam`, `.formParam`,`.pathParam`,`.status` and more to build out a skeleton of the information that needs to be provided in the javadoc comment. Currently this process of interpretting the content of an endpoint can only be done on a lambda expression. This is due to a current system limitation that it can not currently locate a function other than the one it is currently and will hopefully be resolved over time. Also similar to 1, a call to these methods will be ignored if the parameter to the call is a variable not a constant.
    3. The built-in css file is currently extremely limited. It basically has nothing in it. Currently, the system basically generates a skeletal html file. If you would like better formatting, then you will need to supply a css file as a parameter to the system. Almost every tag in the generated html is marked with a classname that you can use in building a css file.  Overtime, the plan is to build a default css file.

## Credits
* This library makes use of the [Java Parser](http://javaparser.org) AST Generator
* The HTML is generated with a modified version of the HTML generator used internally by java for javadoc commenting
