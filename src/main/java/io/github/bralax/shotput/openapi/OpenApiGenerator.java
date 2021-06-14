package io.github.bralax.shotput.openapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.bralax.shotput.Config;
import io.github.bralax.shotput.endpoint.Endpoint;
import io.github.bralax.shotput.endpoint.Parameter;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.servers.Server;

/** Class responsible for generating openapi/swagger specs.
 * @author Brandon Lax
 */
public class OpenApiGenerator {

    private Config config;

    public OpenApiGenerator(Config config) {
        this.config = config;
    }

    public OpenAPI generate(List<Endpoint> endpoints) {
        OpenAPI api = new OpenAPI()
                .info(new Info().description(config.description))
                .addServersItem(new Server().url(config.baseUrl).description(config.title));
        Map<String, List<Endpoint>> grouped = groupEndpointsByPath(endpoints);
        for (Map.Entry<String, List<Endpoint>> group : grouped.entrySet()) {
            PathItem item = new PathItem();
            for (Endpoint endpoint: group.getValue()) {
                Operation opt = new Operation();
                opt.addTagsItem(endpoint.getGroup());
                opt.setSummary(endpoint.getTitle());
                opt.setDescription(endpoint.getDescription());
                opt.requestBody(createBodyForEndpoint(endpoint));
                switch (endpoint.getType()) {
                    case "GET":
                        item.setGet(opt);
                        break;
                    case "POST":
                        item.setPost(opt);
                        break;
                    case "PATCH":
                        item.setPatch(opt);
                        break;
                    case "DELETE":
                        item.setDelete(opt);
                        break;
                }
            }
            api.path(group.getKey(), item);
        }
        return api;
        
        
    }

    public Map<String, List<Endpoint>> groupEndpointsByPath(List<Endpoint> endpoints) {
        Map<String, List<Endpoint>> grouped = new HashMap<>();
        for (Endpoint endpoint : endpoints) {
            if (grouped.containsKey(endpoint.getEndpoint())) {
                grouped.get(endpoint.getEndpoint()).add(endpoint);
            } else {
                List<Endpoint> group = new ArrayList<>();
                group.add(endpoint);
                grouped.put(endpoint.getEndpoint(), group);
            }
        }
        return grouped;
    }

    public RequestBody createBodyForEndpoint(Endpoint endpoint) {
        RequestBody body = new RequestBody();
        Content content = new Content();
        MediaType media = new MediaType();
        Schema<Object> schema = new Schema<>();
        for (Parameter param: endpoint.formParams()) {
            Schema<Object> paramSchema = new Schema<Object>();
            paramSchema.setType(param.getType());
            paramSchema.setDescription(param.getDescription());
            paramSchema.setName(param.getName());
            if (param.getRequired()) {
                paramSchema.addRequiredItem(param.getName());
            }
            schema.addProperties(param.getName(), paramSchema);
        }
        media.setSchema(schema);
        content.put("*/*", media);
        body.content(content);
        return body;
    }

}