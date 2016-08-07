package com.digitalsanctum.lambda.server.filter;

import jersey.repackaged.com.google.common.collect.ImmutableSet;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * If the Content-Type header is present but doesn't have a value, then set it to application/json.
 * This is a hack around the fact that AWS SDK creates a the Content-Type header with no value which Jetty doesn't like.
 *
 * @author Shane Witbeck
 * @since 8/5/16
 */
public class AWSFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(AWSFilter.class);

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // no op
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    Request request = (Request) servletRequest;
    filterChain.doFilter(new HttpServletRequestWrapper(request) {
      @Override
      public String getHeader(String name) {
        String origValue = super.getHeader(name);
        if (CONTENT_TYPE.equals(name) && (origValue == null || Objects.equals(origValue, ""))) {
          log.warn("missing value for Content-Type header; setting to {}", APPLICATION_JSON);
          return APPLICATION_JSON;
        }
        return origValue;
      }

      @Override
      public Enumeration<String> getHeaders(String name) {
        Enumeration<String> headers = super.getHeaders(name);
        if (CONTENT_TYPE.equals(name)) {
          boolean missingValue = false;
          try {
            if (Objects.equals(headers.nextElement(), "")) {
              missingValue = true;
            }
          } catch (NoSuchElementException e) {
            missingValue = true;
          }          
          if (missingValue) {
            return Collections.enumeration(ImmutableSet.of(APPLICATION_JSON));
          }          
        }
        return headers;
      }
      
      @Override
      public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        if (names.contains(CONTENT_TYPE)) {
          return super.getHeaderNames();
        }
        names.add(CONTENT_TYPE);
        return Collections.enumeration(names);
      }
    }, servletResponse);
  }

  @Override
  public void destroy() {
    // no op
  }
}
