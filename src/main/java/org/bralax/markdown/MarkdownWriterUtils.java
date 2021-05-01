package org.bralax.markdown;

import java.util.Map;

import org.bralax.endpoint.Endpoint;

import static java.util.Map.entry;

public class MarkdownWriterUtils {
    public static Map<String, String> methodColor = Map.ofEntries(
        entry("GET", "green"),
        entry("HEAD", "darkgreen"),
        entry("POST", "black"),
        entry("PUT", "darkblue"),
        entry("PATCH", "purple"),
        entry("DELETE", "red"),
        entry("OPTIONS", "grey")
    );

    public String methodToColor(String method) {
        return MarkdownWriterUtils.methodColor.get(method);
    }

    public String fullNameForArr(String name, String type) {
        while (type.endsWith("[]")) {//\Str::endsWith($baseType, '[]')) {
            name = name + ".0";
            type = type.substring( 0, type.length() -2);
        }
        return name;
    }

    public String fullTypeForArr( String type) {
        while (type.endsWith("[]")) {
            type = type.substring( 0, type.length() -2);
        }
        return type;
    }

    public String getInputType(String baseType) {
        switch(baseType) {
            case "Float":
            case "Int":
                return "number";
            case "File":
                return "file";
            default:
                return "text";
        }
    }

    public String generateRequestForEndpoint(String language, Endpoint endpoint) {
        return "";
    }
}
