package io.github.bralax.shotput.endpoint;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.ArrayList;
import java.util.HashMap;

/** Class representing an endpoint.
 * @author Brandon Lax
 */
public class Endpoint {
   /** The endpoint type */ 
   private Type type;

   /** The endpoint path. */
   private String endpoint;

   /** A list of all path paramters. */
   private List<Parameter> pathParams;

   /** A list of all query paramters. */
   private List<Parameter> queryParams;
   
   /** A list of all form paramters. */
   private List<Parameter> formParams;

   /** A Map of all body paramters. (the form paramters grouped) */
   private Map<String, BodyParameter> bodyParams;
   
   /** A list of all header paramters. */
   private List<Parameter> headerParams;

   /** A list of all response headers. */
   private List<Parameter> responseHeaders;
   
   /** A list of all response fields. */
   private List<Parameter> responseFields;
   
   /** A list of all example responses. */
   private List<Response> exampleResponses;
   
   /** The mime type of the response. */
   private String responseType;
   
   /** A description of the response. */
   private String responseDescription;
   
   /** Whether you must be authenticated to access this endpoint. */
   private boolean authenticated;

   /** The group this endpoint is in. */
   private String group;

   /** A title of the endpoint. */
   private String title;

   /** A description of the endpoint. */
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
      this.responseFields = new ArrayList<>();
      //this.responseStatuses = new ArrayList<>();
      this.responseHeaders = new ArrayList<>();
      this.exampleResponses = new ArrayList<>();
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
      this.responseFields = new ArrayList<>();
      //this.responseStatuses = new ArrayList<>();
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

   public void addResponseField(Parameter param) {
      this.responseFields.add(param);
   }
   /*public void addResponseStatus(Parameter param) {
      this.responseStatuses.add(param);
   }*/

   public void addResponseHeader(Parameter param) {
      this.responseHeaders.add(param);
   }

   public void addExampleResponse(Response param) {
      this.exampleResponses.add(param);
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
      return this.responseFields.size();
   }

   public int exampleResponseLength() {
      return this.exampleResponses.size();
   }

   /*public int responseStatusLength() {
      return this.responseStatuses.size();
   }*/

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

   /*public Parameter responseStatus(int i) {
      return this.responseStatuses.get(i);
   }*/

   public Parameter responseHeader(int i) {
      return this.responseHeaders.get(i);
   }

   public Parameter responseField(int i) {
      return this.responseFields.get(i);
   }

   public Response exapleResponse(int i) {
      return this.exampleResponses.get(i);
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

   public Parameter[] responseFields() {
      return this.responseFields.toArray(new Parameter[this.responseFields.size()]);
   }

   public Response[] exampleResponses() {
      return this.exampleResponses.toArray(new Response[this.exampleResponses.size()]);
   }
   /*public Parameter[] responseStatus() {
      return this.responseStatuses.toArray(new Parameter[this.responseStatuses.size()]);
   }*/

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
      "Endpoint Title: " + this.title + "\n" +
      "Endpoint Description: " + this.description  + "\n" +
      "Path Parameters: " + this.pathParams + "\n" + 
      "Query Parameters: " + this.queryParams + "\n" +
      "Form Parameters" + this.formParams + "\n" + 
      "Body Parameters" + this.bodyParams+ "\n" + 
      "Request Headers" + this.headerParams + "\n" + 
      "Response Headers" + this.responseHeaders + "\n" +
      "Response Fields: " + this.responseFields + "\n" + 
      "Example Responses: " + this.exampleResponses
      + "}";
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