package com.infilos.spring.config

import com.infilos.relax.Json4s
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfigure {

  @Autowired
  private val context: ApplicationContext = null

  def modelResolver(): ModelResolver = new ModelResolver(Json4s.underMapper())

  def buildOpenApi(): OpenAPI = {
    val appname = if (StringUtils.isNotBlank(context.getApplicationName)) context.getApplicationName else "Server"
    new OpenAPI().info(new Info().title(appname))
  }
}
