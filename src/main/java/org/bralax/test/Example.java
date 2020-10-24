package org.bralax.test;
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
         @endpoint /
         @endpointType GET 
         @endpointQueryParam age the age of the person
         @endpointPathParam  name the name of the person
         @endpointStatus 200 success
         @endpointStatus 500 error
       */
      app.get("/", ctx -> {
         ctx.status(200); 
         ctx.json(test);
      });

      /** This is a test javadoc comment.
         @endpoint test
         @endpointType POST 
         @endpointFormParam age the age of the person
         @endpointFormParam  name the name of the person
         @endpointStatus 200 success
       */
      app.post("/test", ctx -> {
         ctx.status(200); 
         ctx.result("Text");
      });
   }
}
