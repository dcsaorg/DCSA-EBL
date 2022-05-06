package ebl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TestUtil {

  private static InputStream openStream(String resource) throws IOException {
    URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
    if (url == null) {
      throw new IllegalStateException("Cannot find json file " + resource);
    }
    return url.openStream();
  }

  public static String loadFileAsString(String resource) {
    return parseResourceWithStream(
        resource,
        inputStream -> {
          Reader dataInputStream = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
          StringBuilder stringBuilder = new StringBuilder();
          char[] buffer = new char[4096];
          int read;
          while ((read = dataInputStream.read(buffer)) > 0) {
            stringBuilder.append(buffer, 0, read);
          }
          return stringBuilder.toString().trim();
        });
  }

  @SneakyThrows
  private static <T> T parseResourceWithStream(
      String classpath, ParserFunction<InputStream, T> reader) {
    try (InputStream inputStream = openStream(classpath)) {
      return reader.apply(inputStream);
    }
  }

  @SneakyThrows
  public static Map<String, Object> jsonToMap(String json) {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> map = mapper.readValue(json, Map.class);
    return map;
  }

  private interface ParserFunction<T, R> {
    R apply(T t) throws Exception;
  }
}
