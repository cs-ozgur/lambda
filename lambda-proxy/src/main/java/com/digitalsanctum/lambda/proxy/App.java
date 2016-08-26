package com.digitalsanctum.lambda.proxy;

import com.digitalsanctum.lambda.Definition;
import com.digitalsanctum.lambda.Executor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class App {

    private Server server;
    private int port;

    public App(int port, String lambdaHandler, int lambdaTimeout) {
        this.port = port;
        
        ServletContextHandler sch = new ServletContextHandler();
        sch.setContextPath("/");

        Executor executor = new Executor(new Definition(lambdaHandler, lambdaTimeout));
        RequestResponseHandlerWrapperServlet handlerWrapperServlet = new RequestResponseHandlerWrapperServlet(executor);
        ServletHolder holder = new ServletHolder(handlerWrapperServlet);
        sch.addServlet(holder, "/*");

        server = new Server(port);
        server.setHandler(sch);
    }

    public void start() throws Exception {
        server.start();
        System.out.println("started on port " + port);
    }

    public void stop() throws Exception {
        server.stop();
        System.out.println("stopped");
    }

    public static void main(String[] args) throws Exception {

        int port = 8080;
        if (args != null && args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        String lambdaHandler = System.getenv("LAMBDA_HANDLER");
        if (lambdaHandler == null) {
            System.err.println("You must set LAMBDA_HANDLER");
            return;
        }
        
        int lambdaTimeout = System.getenv("LAMBDA_TIMEOUT") == null 
            ? 60 
            : Integer.parseInt(System.getenv("LAMBDA_TIMEOUT"));

        System.out.println("LAMBDA_HANDLER=" + lambdaHandler);
        System.out.println("LAMBDA_TIMEOUT=" + lambdaTimeout);
        
        App app = new App(port, lambdaHandler, lambdaTimeout);
        app.start();
    }
}
