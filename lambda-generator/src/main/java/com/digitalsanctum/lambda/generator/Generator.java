package com.digitalsanctum.lambda.generator;

import com.digitalsanctum.lambda.Executor;
import com.digitalsanctum.lambda.model.LambdaConfig;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import javax.lang.model.element.Modifier;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class Generator {

    private final LambdaConfig lambdaConfig;

    public Generator(LambdaConfig lambdaConfig) {
        this.lambdaConfig = lambdaConfig;
    }

    public void exportGatewayJar() {
        System.out.println("exporting gateway jar");
        Path src = lambdaConfig.getApiGatewayModuleRoot().resolve(Paths.get("target", "lambda-api-gateway-jersey-1.0-SNAPSHOT.jar"));
        Path target = lambdaConfig.getLambdaSrcDir().resolve(Paths.get("export", "api.jar"));
        try {
            if (target.toFile().exists()) {
                target.toFile().delete();
            }
            Files.copy(src, target);
        } catch (IOException e) {
            System.err.println("copy failed");
            e.printStackTrace();
        }
        System.out.println("export complete");
    }

    public Generator generateJerseyResource() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        FieldSpec executorField = FieldSpec.builder(Executor.class, "executor")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();

        MethodSpec ctor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(Executor.class, "executor").build())
                .addStatement("this.executor = executor")
                .build();

        int responseStatusCode;
        Class httpMethod;
        if (Objects.equals(lambdaConfig.getHttpMethod(), "POST")) {
            httpMethod = POST.class;
            responseStatusCode = 201;
        }
        else {
            httpMethod = GET.class;
            responseStatusCode = 200;
        }
        Map<String, Class> types = Executor.getRequestHandlerTypes(lambdaConfig.getHandler());
        Class requestType = types.get("request");

        ParameterSpec paramSpec = getParameterSpec(httpMethod, requestType);

        MethodSpec messageMethod = MethodSpec.methodBuilder("message")
                .addAnnotation(httpMethod)
                .addAnnotation(AnnotationSpec.builder(Consumes.class).addMember("value", "$S", MediaType.APPLICATION_JSON).build())
                .addAnnotation(AnnotationSpec.builder(Produces.class).addMember("value", "$S", MediaType.APPLICATION_JSON).build())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(paramSpec)
                .addException(Exception.class)
                .returns(Response.class)
                .addStatement("Object obj = executor.execute(input).getResult()")
                .addStatement("return $T.status($L).entity(obj).build()", Response.class, responseStatusCode)
                .build();

        TypeSpec endpoint = TypeSpec.classBuilder("EntryPoint")
                .addAnnotation(AnnotationSpec.builder(javax.ws.rs.Path.class)
                        .addMember("value", "$S", lambdaConfig.getResourcePath())
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .addField(executorField)
                .addMethod(ctor)
                .addMethod(messageMethod)
                .build();

        JavaFile javaFile = JavaFile.builder("com.digitalsanctum.lambda.api.gateway", endpoint)
                .build();

        javaFile.writeTo(System.out);
        javaFile.writeTo(lambdaConfig.getLambdaSrcDir().resolve(Paths.get("lambda-api-gateway-jersey", "src", "main", "java")));

        return this;
    }

    private Generator invokeMaven(Path pomFile, String... goals) {
        if (pomFile == null) {
            throw new IllegalArgumentException("No path specified for pom.xml");
        }

        System.out.println("invokeMaven pom: " + pomFile + ", goals: " + Arrays.toString(goals));

        InvocationRequest invocationRequest = new DefaultInvocationRequest()
                .setPomFile(pomFile.toFile())
                .setGoals(Arrays.asList(goals));

        Invoker invoker = new DefaultInvoker();
        InvocationResult result = null;
        try {
            result = invoker.execute(invocationRequest);
        } catch (MavenInvocationException e) {
            System.err.println("Error executing Maven goals. " + e.getMessage());
            System.exit(1);
        }
        if (result != null && result.getExitCode() != 0) {
            System.err.println("Maven goal failed with non-zero exit code; pom: "
                    + pomFile.toString() + ", goals: " + Arrays.asList(goals));
            System.exit(1);
        }
        return this;
    }

    private Generator installLambdaCore(Path pomFile, Path coreJar) {
        return invokeMaven(pomFile, "install:install-file -Dfile=" + coreJar.toString() +
                " -DgroupId=com.digitalsanctum.lambda -DartifactId=lambda-core -Dversion=1.0-SNAPSHOT -Dpackaging=jar");
    }

    public Generator installLambdaJar() {
        Path rootPomPath = lambdaConfig.getLambdaSrcDir().resolve("pom.xml");
        return invokeMaven(rootPomPath, "install:install-file -Dfile=" + lambdaConfig.getLambdaJarPath()
                + " -DgroupId=com.foo -DartifactId=lambda -Dversion=1.0 -Dpackaging=jar");
    }

    public Generator compileAndPackageGateway() {
        return invokeMaven(lambdaConfig.getLambdaSrcDir().resolve(Paths.get("lambda-api-gateway-jersey", "pom.xml")), "clean", "package");
    }

    private ParameterSpec getParameterSpec(Class httpMethod, Class requestType) {
        ParameterSpec paramSpec;
        if (httpMethod.equals(POST.class)) {
            paramSpec = ParameterSpec.builder(requestType, "input").build();

        } else if (httpMethod.equals(GET.class)) {
            paramSpec = ParameterSpec.builder(requestType, "input")
                    .addAnnotation(AnnotationSpec.builder(QueryParam.class)
                            .addMember("value", "$S", "input")
                            .build())
                    .build();
        } else {
            throw new IllegalArgumentException("unsupported http method " + httpMethod.getName());
        }
        return paramSpec;
    }
}
