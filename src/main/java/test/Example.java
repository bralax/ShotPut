import io.javalin.Javalin;

public class Example {
   /**
         This is a test javadoc comment;
         @endpoint /{name}/
         @endpointType GET
         @endpointQueryParam age the age of the person
         @endpointPathParam  name the name of the person
   */
   public static void main(String[] args) {
      var test = "String";
      Javalin app = Javalin.create().start(8000);
      /** This is a test javadoc comment.
         @endpoint test kajsdhjvg
         @endpointType GET 
         @endpointQueryParam age the age of the person
         @endpointPathParam  name the name of the person
       */
      app.get("/", ctx -> {ctx.status(200); ctx.result("Yes");});
   }
}
