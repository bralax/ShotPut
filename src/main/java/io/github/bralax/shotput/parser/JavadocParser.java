package io.github.bralax.shotput.parser;

//import java.util.Arrays;
import java.util.List;
//import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.javadoc.JavadocBlockTag;

import io.github.bralax.shotput.Shotput;
import io.github.bralax.shotput.endpoint.Endpoint;
import io.github.bralax.shotput.endpoint.Parameter;
import io.github.bralax.shotput.endpoint.Response;


/**
 * Helpers for parsing the tags in a javadoc into a Endpoint.
 * @author Brandon Lax
 */
public class JavadocParser {
    /**
     * Base method of this class. Responsible for parsing the javadocs.
     * @param endpoint The endpoint to add information to
     * @param tags The javadoc tags to intepret
     */
    public static void parseJavadocTags(Endpoint endpoint, List<JavadocBlockTag> tags) {
        endpoint.setAuthenticated(false);
        for (JavadocBlockTag tag: tags) {
            String content = tag.getContent().toText().strip();
            String tagName = tag.getTagName();
            switch (tagName) {
                case "endpoint":
                    endpoint.setEndpoint(content);
                    break;
                case "type":
                    endpoint.setType(Endpoint.Type.valueOf(content));
                    break;
                case "authenticated":
                    endpoint.setAuthenticated(true);
                    break;
                case "group":
                    endpoint.setGroup(content);
                    break;
                case "responseType":
                    endpoint.setResponseType(content);
                    break;
                case "response":
                    parseResponseJavadoc(endpoint, tag);
                    break;
                default: 
                    parseParameterJavadoc(endpoint, tag);
                    break;
            }
        }
    }

    /**
     * Method for parsing the response javadoc
     * @param endpoint The endpoint to add information to
     * @param tag The javadoc tag to intepret
     */
    public static void parseResponseJavadoc(Endpoint endpoint, JavadocBlockTag tag) {
        String unparsedContent = tag.getContent().toText().strip();
        Integer code;
        String reason, example;
        Pattern pattern = Pattern.compile("(\\d+)?\\s*(\\\"[\\w\\s]+\\\")?\\s*([\\s\\S]*)");
        Matcher matcher = pattern.matcher(unparsedContent);
        if (matcher.find() && matcher.groupCount() == 3) {
            code = Integer.parseInt(matcher.group(1));
            reason = matcher.group(2);
            example = matcher.group(3);
        } else {
            return;
        }
        Response resp = new Response(code, reason, example);
        switch (tag.getTagName()) {
            case "response":
                endpoint.addExampleResponse(resp);
                break;
        }
    }

    /**
     * Method for parsing any javadoc with a parameter
     * @param endpoint The endpoint to add information to
     * @param tag The javadoc tag to intepret
     */
    public static void parseParameterJavadoc(Endpoint endpoint, JavadocBlockTag tag) {
        String unparsedContent = tag.getContent().toText().strip();
        String paramName, paramType, content, example;
        boolean required = false;
        Pattern pattern = Pattern.compile("(\\w+?)\\s+(Array|Boolean|File|Float|Int|Object|String)?\\s+(required\\s+)?([\\s\\S]*)");
        Matcher matcher = pattern.matcher(unparsedContent);
        if (matcher.find() && matcher.groupCount() == 4) {
            paramName = matcher.group(1);
            paramType = matcher.group(2);
            required = matcher.group(3) != null ? true : false;
            content = matcher.group(4);
            example = null;
            if (content != null) {
                Pattern examplePattern = Pattern.compile("(.*)\\bExample:\\s*([\\s\\S]+)\\s*", Pattern.DOTALL);
                Matcher exampleMatcher = examplePattern.matcher(content);
                if (exampleMatcher.find() && exampleMatcher.groupCount() == 2) {
                    content = exampleMatcher.group(1);
                    example = exampleMatcher.group(2);
                }
            } 
        } else {
            return;
        }
        Parameter newParam = new Parameter(paramName, content);
        newParam.setRequired(required);
        newParam.setType(paramType);
        newParam.setExample(example);
        switch (tag.getTagName()) {
            case "queryParam":
                    endpoint.addQueryParam(newParam);
                    break;
                case "pathParam":
                    newParam.setRequired(true);
                    endpoint.addPathParam(newParam);
                    break;
                case "bodyParam":
                    endpoint.addFormParam(newParam);
                    break;
                case "requestHeader":
                    endpoint.addHeaderParam(newParam);
                    break;
                case "responseHeader":
                    endpoint.addResponseHeader(newParam);
                    break;
                case "responseField":
                    endpoint.addResponseField(newParam);
                    break;
        }
    }
}
