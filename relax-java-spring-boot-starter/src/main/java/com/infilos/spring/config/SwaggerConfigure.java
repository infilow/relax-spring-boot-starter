package com.infilos.spring.config;

import com.infilos.relax.Json;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

@Configuration
public class SwaggerConfigure {

  @Autowired private ApplicationContext context;

  @Bean
  @Primary
  public ModelResolver modelResolver() {
    return new ModelResolver(Json.underMapper());
  }

  @Bean
  public OpenAPI buildOpenApi() {
    String appname =
        StringUtils.isNotBlank(context.getApplicationName())
            ? context.getApplicationName()
            : "Server";
    return new OpenAPI().info(new Info().title(appname));
  }
}
