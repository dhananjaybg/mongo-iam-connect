package org.example;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


public class lambdaHandler implements RequestHandler<Object, Response> {
    public lambdaHandler() {

    }
    public Response handleRequest(Object input, Context context) {
        String out_strx = "";
        StsTest obj = new StsTest();
        out_strx = obj.fetch();
        return new Response(out_strx, 200);
    }
}
