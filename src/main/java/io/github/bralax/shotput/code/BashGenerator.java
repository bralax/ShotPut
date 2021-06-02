package io.github.bralax.shotput.code;

import io.github.bralax.shotput.endpoint.Endpoint;
import io.github.bralax.shotput.endpoint.Parameter;

public class BashGenerator extends SampleCodeGenerator {

    @Override
    public String getType() {
        return "bash";
    }

    @Override
    public String generate(String baseUrl, Endpoint endpoint) {
        StringBuilder builder = new StringBuilder();
        builder.append("```BASH").append("\n");
        builder.append("curl -X ").append(endpoint.getType()).append(" \\ ").append("\n");
        if (endpoint.getType().equals("GET")) {
            builder.append("-G ");
        }
        builder.append(generateFullUrl(baseUrl, endpoint));
        builder.append("\"");
        if (endpoint.headerParamLength() > 0) {
            builder.append(" \\ ").append("\n");
            for (int i = 0; i < endpoint.headerParamLength(); i++) {
                Parameter header = endpoint.headerParam(i);
                builder.append(" -H \"").append(header.getName()).append(":").append(this.generateDefaultValue(header)).append("\"");
                if (i < endpoint.headerParamLength() - 1) {
                    builder.append(" \\ ").append("\n");
                }
            }
        }
        builder.append("\n").append("```");
        return builder.toString();
    }

    
    

}
