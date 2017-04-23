package com.digitalsanctum.lambda;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
* 
* TODO local file paths here
* 
 * @author Shane Witbeck
 * @since 4/18/17
 */
public class Configuration {

  public static final Path ROOT_DIR = Paths.get(System.getProperty("user.home"), ".lambda");
  public static final String CONFIG_SUFFIX = "-config.json";
  public static final String CODE_LOC_SUFFIX = "-code-loc.json";
  public static final String CODE_SUFFIX = "-code.jar";
  public static final int CONTAINER_AVAILABILITY_SLEEP_PERIOD = 500;
  public static final int CONTAINER_AVAILABILITY_TIMEOUT = 10000;
  public static final String MAPPING_SUFFIX = "-mappings.json";
}
