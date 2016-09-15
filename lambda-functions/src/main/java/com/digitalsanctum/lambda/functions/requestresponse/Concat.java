package com.digitalsanctum.lambda.functions.requestresponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.digitalsanctum.lambda.functions.model.ConcatRequest;
import com.digitalsanctum.lambda.functions.model.ConcatResponse;

public class Concat implements RequestHandler<ConcatRequest, ConcatResponse> {
    public ConcatResponse handleRequest(ConcatRequest request, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log(request.toString());
        ConcatResponse response = new ConcatResponse();
        response.setMessage(request.getFirstName() + " " + request.getLastName());
        logger.log(response.toString());
        return response;
    }
}
