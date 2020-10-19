import java.util.List;
import java.util.ArrayList;

public class Endpoint {
   private Type type;
   private String endpoint;
   private List<Parameter> pathParams;
   private List<Parameter> queryParams;
   private List<Parameter> formParams;
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
   }
   
   public Endpoint(Type type, String endpoint) {
      this.type = type;
      this.endpoint = endpoint;
      this.responseType = "";
      this.description = "";
      this.pathParams = new ArrayList<>();
      this.queryParams = new ArrayList<>();
      this.formParams = new ArrayList<>();
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
   
   public String setDescription() {
      return this.description;
   }




   public enum Type {
      GET,
      POST,
      PATCH,
      DELETE
   }
}