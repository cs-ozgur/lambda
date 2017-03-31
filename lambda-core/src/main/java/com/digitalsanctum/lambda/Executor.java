package com.digitalsanctum.lambda;

import com.amazonaws.protocol.json.SdkStructuredPlainJsonFactory;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.JsonUnmarshallerContextImpl;
import com.digitalsanctum.lambda.unmarshallers.DynamodbEventUnmarshaller;
import com.digitalsanctum.lambda.unmarshallers.KinesisEventUnmarshaller;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Executor implements ResultProvider {

  private static final Logger log = LoggerFactory.getLogger(Executor.class);

  private static final ObjectMapper mapper = new ObjectMapper();
  private final Definition definition;
  private Object result;

  public Executor(Definition definition) {
    this.definition = definition;
  }

  @SuppressWarnings("unchecked")
  public ResultProvider execute(final String inputJson) throws Exception {

    log.info("inputJson: {}", inputJson);

    if (this.definition.getHandler().contains("::")) {

      Class cls = Class.forName(this.definition.getHandlerClass());
      Method[] declaredMethods = cls.getDeclaredMethods();
      Method selectedMethod = null;
      for (Method declaredMethod : declaredMethods) {
        if (declaredMethod.getName().equals(this.definition.getHandlerMethod())) {
          selectedMethod = declaredMethod;
          break;
        }
      }
      if (selectedMethod == null) {
        throw new RuntimeException(String.format("Handler method '%s' not found in class '%s'",
            this.definition.getHandlerMethod(), this.definition.getHandlerClass()));
      }

      final Object obj = cls.newInstance();
      final Method method = cls.getDeclaredMethod(this.definition.getHandlerMethod(), selectedMethod.getParameterTypes());

      Class[] paramClasses = method.getParameterTypes();
      Set<Class> paramClassSet = new HashSet<>(asList(paramClasses));

      Object inputObj;
      if (paramClassSet.contains(KinesisEvent.class)) {

        JsonParser jsonParser = SdkStructuredPlainJsonFactory.JSON_FACTORY.createParser(inputJson);
        JsonUnmarshallerContext jsonUnmarshallerContext = new JsonUnmarshallerContextImpl(jsonParser,
            SdkStructuredPlainJsonFactory.JSON_SCALAR_UNMARSHALLERS, null);
        inputObj = KinesisEventUnmarshaller.getInstance().unmarshall(jsonUnmarshallerContext);

      } else if (paramClassSet.contains(DynamodbEvent.class)) {

        JsonParser jsonParser = SdkStructuredPlainJsonFactory.JSON_FACTORY.createParser(inputJson);
        JsonUnmarshallerContext jsonUnmarshallerContext = new JsonUnmarshallerContextImpl(jsonParser,
            SdkStructuredPlainJsonFactory.JSON_SCALAR_UNMARSHALLERS, null);
        inputObj = DynamodbEventUnmarshaller.getInstance().unmarshall(jsonUnmarshallerContext);

      } else {
        throw new UnsupportedOperationException("Unsupported input object type");
      }

      invoke(inputObj, obj, method);

    } else {
      Class cls = Class.forName(this.definition.getHandler());
      final Object obj = cls.newInstance();

      Map<String, Class> handlerTypes = getRequestHandlerTypes(this.definition.getHandler());

      Class requestClass = handlerTypes.get("request");

      Object inputObj = mapper.readValue(inputJson, requestClass);

      invoke(inputObj, obj, cls.getDeclaredMethod("handleRequest", requestClass, Context.class));
    }

    return this;
  }

  private static Map<String, Class> getRequestHandlerTypes(String handler)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {

    Map<String, Class> types = new HashMap<>();
    Class cls = Class.forName(handler);
    final Object obj = cls.newInstance();
    if (obj instanceof RequestHandler) {
      Type[] interfaces = cls.getGenericInterfaces();
      // TODO don't assume RequestHandler is the first/only interface
      ParameterizedType firstInterface = (ParameterizedType) interfaces[0];
      if (firstInterface.getActualTypeArguments().length == 2) {
        Class requestClass = (Class) firstInterface.getActualTypeArguments()[0];
        types.put("request", requestClass);

        Class responseClass = (Class) firstInterface.getActualTypeArguments()[1];
        types.put("response", responseClass);
      } else {
        throw new RuntimeException("unexpected number of type args " + firstInterface.getActualTypeArguments().length);
      }
    } else {
      throw new RuntimeException(obj.getClass() + " does not implement RequestHandler");
    }

    return types;
  }

  @Override
  public Object getResult() {
    return this.result;
  }

  @Override
  public void setResult(Object result) {
    this.result = result;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("definition = " + definition)
        .add("mapper = " + mapper)
        .add("result = " + result)
        .toString();
  }

  private static class FunctionRunnable implements Runnable {

    private final LambdaLogger logger;
    private final Context context;
    private final Object input;
    private final Object functionInstance;
    private final Method functionMethod;
    private final ResultProvider resultProvider;

    FunctionRunnable(Context context, Object input, Object functionInstance, Method functionMethod, ResultProvider resultProvider) {
      this.context = context;
      this.logger = context.getLogger();
      this.input = input;
      this.functionInstance = functionInstance;
      this.functionMethod = functionMethod;
      this.resultProvider = resultProvider;
    }

    @Override
    public void run() {
      logger.log(toString());
      try {
        Object result;
        if (functionMethod.getParameterCount() == 2) {
          result = functionMethod.invoke(functionInstance, input, context);
        } else {
          result = functionMethod.invoke(functionInstance, input);
        }
        resultProvider.setResult(result);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        logger.log("Error invoking function");
        e.printStackTrace();
      }
    }


    @Override
    public String toString() {
      return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
          .add("context = " + context)
          .add("functionInstance = " + functionInstance)
          .add("functionMethod = " + functionMethod)
          .add("input = " + input)
          .add("logger = " + logger)
          .add("resultProvider = " + resultProvider)
          .toString();
    }
  }

  private void invoke(final Object input, final Object functionInstance, final Method functionMethod) {
    final Context context = this.definition.getContext();
    final LambdaLogger logger = context.getLogger();
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final FunctionRunnable functionRunnable = new FunctionRunnable(context, input, functionInstance, functionMethod, this);
    Future future = executor.submit(functionRunnable);

    int timeout = this.definition.getTimeout();

    // start mimic of Amazon Lambda invocation behavior
    logger.log("START request: " + context.getAwsRequestId());
    try {
      future.get(timeout, SECONDS);
    } catch (TimeoutException e) {
      future.cancel(true);
      logger.log("<< TIMED OUT >>");
      e.printStackTrace();
    } catch (Exception e) {
      future.cancel(true);
      logger.log("<< EXCEPTION >>");
      e.printStackTrace();
    } finally {
      executor.shutdownNow();
    }
    logger.log("END request: " + context.getAwsRequestId());
  }


}
