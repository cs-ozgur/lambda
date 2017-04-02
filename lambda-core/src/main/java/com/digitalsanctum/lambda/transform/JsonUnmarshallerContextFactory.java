package com.digitalsanctum.lambda.transform;

import com.amazonaws.http.HttpResponse;
import com.amazonaws.protocol.json.SdkStructuredPlainJsonFactory;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.JsonUnmarshallerContextImpl;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 4/2/17
 */
public class JsonUnmarshallerContextFactory {

  private static JsonFactory jsonFactory = new JsonFactory();

  public JsonUnmarshallerContext getJsonUnmarshallerContext(InputStream body) throws Exception {
    JsonParser jsonParser = jsonFactory.createParser(body);
    return getJsonUnmarshallerContext(jsonParser);
  }

  public JsonUnmarshallerContext getJsonUnmarshallerContext(String body) throws Exception {
    InputStream is = new ByteArrayInputStream(body.getBytes());
    return getJsonUnmarshallerContext(is);
  }

  public JsonUnmarshallerContext getJsonUnmarshallerContext(JsonParser jsonParser) throws Exception {
    return getJsonUnmarshallerContext(jsonParser, null);
  }

  public JsonUnmarshallerContext getJsonUnmarshallerContext(JsonParser jsonParser,
                                                            Map<String, String> headers) throws Exception {
    HttpResponse httpResponse = new HttpResponse(null, null);

    if (headers != null) {
      for (Map.Entry<String, String> header : headers.entrySet()) {
        httpResponse.addHeader(header.getKey(), header.getValue());
      }
    }

    return new JsonUnmarshallerContextImpl(jsonParser,
        SdkStructuredPlainJsonFactory.JSON_SCALAR_UNMARSHALLERS,
        SdkStructuredPlainJsonFactory.JSON_CUSTOM_TYPE_UNMARSHALLERS,
        httpResponse);
  }
}
