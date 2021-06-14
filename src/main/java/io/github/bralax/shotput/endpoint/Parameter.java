package io.github.bralax.shotput.endpoint;

/**
 * Class representing a parameter on an endpoint.
 */
public class Parameter {
   /** The parameter name. */
   private String name;

   /** The paramter description. */
   private String description;

   /** The parameter type. */
   private String type;

   /** An example value for the parameter */
   private String example;

   /** Whether the parameter is mandatory */
   private boolean required;
   
   public Parameter(String name, String description) {
      this(name, description, "String", false);
   }

   public Parameter(String name, String description, String type, boolean required) {
      this.name = name;
      this.description = description;
      this.required = required;
   }
   
   public String getName() {
      return name;
   }
   
   public String getDescription() {
      return description;
   }

   public String getType() {
      return type;
   }

   public String getExample() {
      return example;
   }
   
   public boolean getRequired() {
      return required;
   }

   public void setName(String name) {
       this.name = name;
   }
   
   public void setDescription(String description) {
       this.description = description;
   }

   public void setType(String type) {
      this.type = type;
  }

   public void setRequired(boolean req) {
      this.required = req;
   }

   public void setExample(String example) {
      this.example = example;
   }
   
   @Override
   public String toString() {
      return "{Name: "+this.name+", Type: "+this.type+", Required: "+this.required+", Description: "+this.description+", Example: "+ this.example +"}";
   }

}