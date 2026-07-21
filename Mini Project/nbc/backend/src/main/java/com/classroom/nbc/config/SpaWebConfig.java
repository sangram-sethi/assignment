package com.classroom.nbc.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serves the bundled Angular single-page application from {@code classpath:/static}
 * and forwards client-side (deep-link) routes to {@code index.html} so that a
 * browser refresh on e.g. {@code /dashboard} still loads the app.
 *
 * <p>Real files (hashed JS/CSS, assets) are served directly; anything else that is
 * not an API, docs or Swagger path falls back to the SPA shell.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    private static final ClassPathResource INDEX = new ClassPathResource("static/index.html");

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location)
                            throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        // Let API, OpenAPI docs and Swagger requests 404 normally.
                        if (resourcePath.startsWith("api/")
                                || resourcePath.startsWith("v3/")
                                || resourcePath.startsWith("swagger-ui")) {
                            return null;
                        }
                        // Everything else is an Angular route -> serve the SPA shell.
                        return INDEX.exists() ? INDEX : null;
                    }
                });
    }
}
