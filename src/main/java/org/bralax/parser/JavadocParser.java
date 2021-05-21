package org.bralax.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.github.javaparser.javadoc.JavadocBlockTag;

import org.bralax.endpoint.Endpoint;
import org.bralax.endpoint.Parameter;


public class JavadocParser {
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
                case "group":
                    endpoint.setGroup(content);
                case "responseType":
                    endpoint.setResponseType(content);
                    break;
                default: 
                    parseParameterJavadoc(endpoint, tag);
            }
        }
    }

    public static void parseParameterJavadoc(Endpoint endpoint, JavadocBlockTag tag) {
        String unparsedContent = tag.getContent().toText().strip();
        Scanner parser = new Scanner(unparsedContent);
        String paramName, paramType, content;
        boolean required = false;
        if (parser.hasNext()) {
            paramName = parser.next();
            if (parser.hasNext()) {
                String next = parser.next();
                if (isValidParameterType(next)) {
                    paramType = next;
                    if (parser.hasNext()) {
                        next = parser.next();
                        if (next.equalsIgnoreCase("required")) {
                            required = true;
                            content = getContent(parser);
                        } else {
                            content = next + " " +  getContent(parser);
                        }
                    } else {
                        content = "";
                    }
                } else if (next.equalsIgnoreCase("Required")) {
                    paramType = "String";
                    required = true;
                    content = getContent(parser);
                } else {
                    paramType = "String";
                    content = next + " " + getContent(parser);
                }
            } else {
                paramType = "String";
                content = "";
            }
        } else {
            parser.close();
            return;
        }
        parser.close();
        Parameter newParam = new Parameter(paramName, content);
        newParam.setRequired(required);
        newParam.setType(paramType);
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
                case "responseStatus":
                    endpoint.addResponseStatus(newParam);
                    break;
        }
    }

    private static boolean isValidParameterType(String type) {
        if (type.endsWith("[]")) {
            type = type.substring(0, type.length()-2);
        }
        String[] types = {"Array", "Boolean", "File", "Float", "Int", "Object", "String"};
        return Arrays.binarySearch(types, type) >= -1;
    }

    private static String getContent(Scanner scrn) {
        StringBuilder builder = new StringBuilder();
        while(scrn.hasNextLine()) {
            builder.append(scrn.nextLine()).append("\n");
        }
        return builder.toString();
    }
}
