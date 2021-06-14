package io.github.bralax.shotput.endpoint;

/** Class representing an example response.
 * @author Brandon Lax
 */
public class Response {
    
    private int statusCode;
    private String explantion;
    private String example;

    public Response(int status, String expl, String exampl) {
        this.statusCode = status;
        this.explantion = expl;
        this.example = exampl;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getExplanation() {
        return this.explantion;
    }

    public String getExample() {
        return this.example;
    }

    public void setStatusCode(int code) {
        this.statusCode = code;
    }

    public void setExplanation(String expl) {
        this.explantion = expl;
    }

    public void gstExample(String exmpl) {
        this.example = exmpl;
    }

    @Override
   public String toString() {
      return "{Code: "+this.statusCode+", Explanation: "+this.explantion+", Example: "+this.example+"}";
   }

}
