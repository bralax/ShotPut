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
      String[] test = {"String"};
      Javalin app = Javalin.create().start(8000);
      /** This is a test javadoc comment.
         @endpoint test
         @endpointType GET 
         @endpointQueryParam age the age of the person
         @endpointPathParam  name the name of the person
         @endpointStatus 200 success
       */
      app.get("/", ctx -> {
         ctx.status(200); 
         ctx.json(test);
      });
   }
}
