import java.util.List;
import java.util.ArrayList;

public class Endpoint {
   private Type type;
   private String endpoint;
   private List<Parameter> pathParams;
   private List<Parameter> queryParams;
   private List<Parameter> formParams;
   private List<Parameter> headerParams;
   private List<Parameter> responseHeaders;
   private List<Parameter> responseStatuses;
   private String responseType;
   private String responseDescription;

   private String description;
   
   public Endpoint() {
      this.type = null;
      this.endpoint = "";
      this.responseType = "";
      this.description = "";
      this.pathParams = new ArrayList<>();
      this.queryParams = new ArrayList<>();
      this.formParams = new ArrayList<>();
      this.headerParams = new ArrayList<>();
      this.responseStatuses = new ArrayList<>();
      this.responseHeaders = new ArrayList<>();
   }
   
   public Endpoint(Type type, String endpoint) {
      this.type = type;
      this.endpoint = endpoint;
      this.responseType = "";
      this.description = "";
      this.pathParams = new ArrayList<>();
      this.queryParams = new ArrayList<>();
      this.formParams = new ArrayList<>();
      this.headerParams = new ArrayList<>();
      this.responseStatuses = new ArrayList<>();
      this.responseHeaders = new ArrayList<>();
   }
   
   public void addPathParam(Parameter param) {
      this.pathParams.add(param);
   }
   
   public void addQueryParam(Parameter param) {
      this.queryParams.add(param);
   }
   
   public void addFormParam(Parameter param) {
      this.formParams.add(param);
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

   public void setResponseDescription(String description) {
      this.responseDescription = description;
   }
   
   public String getResponseDescription() {
      return this.responseDescription;
   }

   public String toString() {
      return "{endpoint : {type:"+this.type +  "\n" +
      "Endpoint: " + this.endpoint  + "\n" +
      "Response Type: " + this.responseType + "\n" + 
      "Endpoint Description: " + this.description  + "\n" +
      "Path Parameters: " + this.pathParams + "\n" + 
      "Query Parameters: " + this.queryParams + "\n" +
      "Form Parameters" + this.formParams + "\n" + 
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


   public enum Type {
      GET,
      POST,
      PATCH,
      DELETE
   }
}