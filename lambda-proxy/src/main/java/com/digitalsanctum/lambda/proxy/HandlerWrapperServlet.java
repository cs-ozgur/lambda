package com.digitalsanctum.lambda.proxy;

import com.digitalsanctum.lambda.Executor;
import com.digitalsanctum.lambda.ResultProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Shane Witbeck
 * @since 8/8/16
 */
public class HandlerWrapperServlet extends HttpServlet {

  private final Executor executor;

  public HandlerWrapperServlet(Executor executor) {
    this.executor = executor;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException
  {
    resp.setStatus(HttpStatus.OK_200);
    resp.setContentType("application/json");
    resp.getWriter().write("{\"status\":\"OK\"}");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    StringBuilder jb = new StringBuilder();
    String line;
    BufferedReader reader = req.getReader();
    while ((line = reader.readLine()) != null) {
      jb.append(line);
    }

    try {
      ResultProvider resultProvider = executor.execute(jb.toString());
      Object resultObj = resultProvider.getResult();
      new ObjectMapper().writeValue(resp.getWriter(), resultObj);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
