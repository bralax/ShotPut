package org.bralax.code;

import java.util.Random;

import org.bralax.endpoint.Endpoint;
import org.bralax.endpoint.Parameter;

public abstract class SampleCodeGenerator {
    
    public abstract String getType();
    public abstract String generate(String baseUrl, Endpoint endpoint);

    protected String generateDefaultValue(Parameter param) {
        switch(param.getType()) {
            case "Int":
                return "" + ((int)Math.random() * 10000);
            case "Float":
                return "" + Math.random();
            case "Boolean":
                return "" + (Math.random() > 0.5);
            default:
                return generateRandomString();
        }
    }

    protected String generateRandomString() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
    
        String generatedString = random.ints(leftLimit, rightLimit + 1)
          .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
          .limit(targetStringLength)
          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
          .toString();
    
        return generatedString;
    }

    protected String generateFullUrl(String baseUrl, Endpoint endpoint) {
        String path = endpoint.getEndpoint();
        StringBuilder builder = new StringBuilder();
        builder.append("\"")
               .append(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl)
               .append("/")
               .append(path.startsWith("/") ? path.substring(1, baseUrl.length()) : baseUrl);
        if (endpoint.queryParamLength() > 0) {
            builder.append("?").append(queryParamsToString(endpoint.queryParams()));
        }
        return builder.toString();
    }

    protected String queryParamsToString(Parameter[] params) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < params.length; i++ ) {
            Parameter param = params[i];
            if (i != 0) {
                builder.append("&");
            }
            builder.append(param.getName())
                   .append("=")
                   .append(generateDefaultValue(param));
        }
        return builder.toString();
    }
}
