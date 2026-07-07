package org.northernarc.assessment4.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

/**
 * Serializes {@link org.springframework.data.domain.Page} responses through a
 * stable DTO structure instead of the internal {@code PageImpl}, guaranteeing a
 * consistent JSON contract for paginated endpoints.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class WebConfig {
}
