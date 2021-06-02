package io.github.bralax.shotput.code;


import io.github.bralax.shotput.endpoint.Endpoint;
import io.github.bralax.shotput.endpoint.Parameter;

public class JavaGenerator extends SampleCodeGenerator {

    @Override
    public String getType() {
        return "java";
    }

    @Override
    public String generate(String baseUrl, Endpoint endpoint) {
        StringBuilder builder = new StringBuilder();
        builder.append("```JAVA").append("\n");
        builder.append("Unirest.")
               .append(endpoint.getType().toLowerCase())
               .append("(\"")
               .append(generateFullUrl(baseUrl, endpoint))
               .append("\")");
        addParameters(endpoint.headerParams(), "header", builder);
        addParameters(endpoint.queryParams(), "queryParam", builder);
        addParameters(endpoint.formParams(), "field", builder);
        if (endpoint.getResponseType().equals("json")) {
            builder.append("\n")
                   .append("\t.asJson()");
        } else {
            builder.append("\n")
                   .append("\t.asString()");
        }

        builder.append(";").append("\n").append("```");
        return builder.toString();
    }
    
    public void addParameters(Parameter[] params, String method, StringBuilder builder) {
        for (Parameter param: params) {
            builder.append("\n")
                    .append("\t")
                    .append(".")
                    .append(method)
                    .append("(\"")
                    .append(param.getName())
                    .append("\",\"")
                    .append(generateDefaultValue(param))
                    .append("\")");
        }
    }
}
