package org.bralax.test;
import io.javalin.Javalin;

public class Example {
   public static void main(String[] args) {
      String[] test = {"String"};
      Javalin app = Javalin.create().start(8000);
      /** This is a test javadoc comment.
         @endpoint /
         @type GET 
         @queryParam age the age of the person
         @pathParam  name the name of the person
         @responseStatus 200 success
         @responseStatus 500 error
       */
      app.get("/", ctx -> {
         ctx.status(200); 
         ctx.json(test);
      });

      /** This is a test javadoc comment.
         @endpoint test
         @type POST 
         @formParam age the age of the person
         @formParam  name the name of the person
         @status 200 success
       */
      app.post("/test", ctx -> {
         ctx.status(200); 
         ctx.result("Text");
      });
   }
}
