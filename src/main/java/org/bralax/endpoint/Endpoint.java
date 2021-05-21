package org.bralax.endpoint;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.HashMap;

public class Endpoint { 
   private Type type;
   private String endpoint;
   private List<Parameter> pathParams;
   private List<Parameter> queryParams;
   private List<Parameter> formParams;
   private Map<String, BodyParameter> bodyParams;
   private List<Parameter> headerParams;
   private List<Parameter> responseHeaders;
   private List<Parameter> responseStatuses;
   private List<Parameter> exampleResponse;
   private String responseType;
   private String responseDescription;
   private boolean authenticated;
   private String group;

   private String title;
   private String description;
   
   public Endpoint() {
      this.type = null;
      this.endpoint = "";
      this.responseType = "";
      this.description = "";
      this.title = "";
      this.pathParams = new ArrayList<>();
      this.queryParams = new ArrayList<>();
      this.formParams = new ArrayList<>();
      this.bodyParams = new HashMap<>();
      this.headerParams = new ArrayList<>();
      this.responseStatuses = new ArrayList<>();
      this.responseHeaders = new ArrayList<>();
      this.exampleResponse = new ArrayList<>();
      this.group = "Endpoint";
      this.authenticated = false;
   }
   
   public Endpoint(Type type, String endpoint) {
      this.type = type;
      this.endpoint = endpoint;
      this.responseType = "";
      this.description = "";
      this.title = endpoint;
      this.pathParams = new ArrayList<>();
      this.queryParams = new ArrayList<>();
      this.formParams = new ArrayList<>();
      this.headerParams = new ArrayList<>();
      this.responseStatuses = new ArrayList<>();
      this.responseHeaders = new ArrayList<>();
      this.authenticated = false;
      this.group = "Endpoint";
   }
   
   public void addPathParam(Parameter param) {
      this.pathParams.add(param);
   }
   
   public void addQueryParam(Parameter param) {
      this.queryParams.add(param);
   }
   
   public void addFormParam(Parameter param) {
      this.formParams.add(param);
      String[] splitName = param.getName().split(".");
      if (splitName.length == 1) {
         if (this.bodyParams.containsKey(splitName[0])) {
            this.bodyParams.get(splitName[0]).setParam(param);
         } else {
            this.bodyParams.put(splitName[0], new BodyParameter(param));
         }
      } else if (splitName.length > 1) {
         if (this.bodyParams.containsKey(splitName[0])) {
            this.bodyParams.get(splitName[0]).addChild(param, 1);
         } else {
            BodyParameter body = new BodyParameter(new Parameter(splitName[0], null));
            body.addChild(param, 1);
            this.bodyParams.put(splitName[0], body);
         }
      }
   }

   public void addHeaderParam(Parameter param) {
      this.headerParams.add(param);
   }

   public void addResponseStatus(Parameter param) {
      this.responseStatuses.add(param);
   }

   public void addResponseHeader(Parameter param) {
      this.responseHeaders.add(param);
   }

   public void addExampleResponse(Parameter param) {
      this.exampleResponse.add(param);
   }


   public int pathParamLength() {
      return this.pathParams.size();
   }
   
   public int queryParamLength() {
      return this.queryParams.size();
   }
   
   public int formParamLength() {
      return this.formParams.size();
   }

   public int headerParamLength() {
      return this.headerParams.size();
   }

   public int responseStatusLength() {
      return this.responseStatuses.size();
   }

   public int responseHeaderLength() {
      return this.responseHeaders.size();
   }


   public Parameter pathParam(int i) {
      return this.pathParams.get(i);
   }
   
   public Parameter queryParam(int i) {
      return this.queryParams.get(i);
   }
   
   public Parameter formParam(int i) {
      return this.formParams.get(i);
   }

   public Parameter headerParam(int i) {
      return this.headerParams.get(i);
   }

   public Parameter responseStatus(int i) {
      return this.responseStatuses.get(i);
   }

   public Parameter responseHeader(int i) {
      return this.responseHeaders.get(i);
   }

   public Parameter[] pathParams() {
      return this.pathParams.toArray(new Parameter[this.pathParamLength()]);
   }
   
   public Parameter[] queryParams() {
      return this.queryParams.toArray(new Parameter[this.queryParamLength()]);
   }
   
   public Parameter[] formParams() {
      return this.formParams.toArray(new Parameter[this.formParamLength()]);
   }

   public Parameter[] headerParams() {
      return this.headerParams.toArray(new Parameter[this.formParamLength()]);
   }

   public Parameter[] responseStatus() {
      return this.responseStatuses.toArray(new Parameter[this.responseStatuses.size()]);
   }

   public Parameter[] responseHeaders() {
      return this.responseHeaders.toArray(new Parameter[this.responseHeaders.size()]);
   }

   public  Map<String, BodyParameter> bodyParams() {
      return this.bodyParams;
   }

   
   public String getType() {
      if (this.type != null) {
         return this.type.toString();
      }
      return "";
   }
   
   public void setType(Type type) {
       this.type = type;
   }
   
   public void setType(String type) {
       this.type = Type.valueOf(type);
   }
   
   public String getEndpoint() {
      return this.endpoint;
   }
   
   public void setEndpoint(String endpoint) {
       this.endpoint = endpoint;
   }


   public String getResponseType() {
      return this.responseType;
   }
   
   public void setResponseType(String responseType) {
       this.responseType = responseType;
   }
   
   public void setDescription(String description) {
      this.description = description;
   }
   
   public String getDescription() {
      return this.description;
   }

   public void setTitle(String title) {
      this.title = title;
   }
   
   public String getTitle() {
      return this.title == null || this.title.length() == 0 ? this.endpoint : this.title;
   }

   public void setResponseDescription(String description) {
      this.responseDescription = description;
   }
   
   public String getResponseDescription() {
      return this.responseDescription;
   }

   public boolean getAuthenticated() {
      return this.authenticated;
   }

   public void setAuthenticated(boolean auth) {
      this.authenticated = auth;
   }

   public int authInt() {
      return this.authenticated ? 1 : 0;
   }

   public String getGroup() {
      return this.group;
   }

   public void setGroup(String group) {
      this.group = group;
   }

   public String toString() {
      return "{endpoint : {type:"+this.type +  "\n" +
      "Endpoint: " + this.endpoint  + "\n" +
      "Response Type: " + this.responseType + "\n" + 
      "Endpoint Description: " + this.description  + "\n" +
      "Path Parameters: " + this.pathParams + "\n" + 
      "Query Parameters: " + this.queryParams + "\n" +
      "Form Parameters" + this.formParams + "\n" + 
      "Body Parameters" + this.bodyParams+ "\n" + 
      "Request Headers" + this.headerParams + "\n" + 
      "Response Headers" + this.responseHeaders + "\n" + 
      "Response Status Codes: " + this.responseStatuses + "}";
   }

   public boolean equals(Object a) {
      if (a instanceof Endpoint) {
         Endpoint other = (Endpoint) a;
         return this.type.equals(other.type) && this.endpoint.equals(other.endpoint);
      }
      return false;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type.toString(), this.endpoint);
   }


   public enum Type {
      GET,
      POST,
      PATCH,
      DELETE,
      WS,
      SSE
   }
}