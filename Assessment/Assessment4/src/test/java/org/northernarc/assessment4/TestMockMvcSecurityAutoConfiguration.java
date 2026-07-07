package org.northernarc.assessment4;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;

/**
 * Restores the Spring Security &harr; MockMvc integration that Spring Boot 3
 * provided out of the box via {@code MockMvcSecurityConfiguration}.
 * <p>
 * The Spring Boot 4 {@code spring-boot-webmvc-test} module no longer auto-applies
 * {@link SecurityMockMvcConfigurers#springSecurity()}, which means annotations
 * such as {@code @WithMockUser} are not bridged into MockMvc requests. This
 * test-scoped auto-configuration re-registers that behaviour so method-level
 * security and mock authentication are honoured during integration tests.
 */
@AutoConfiguration
@ConditionalOnClass(SecurityMockMvcConfigurers.class)
public class TestMockMvcSecurityAutoConfiguration {

    @Bean
    MockMvcBuilderCustomizer securityMockMvcBuilderCustomizer() {
        return new SecurityMockMvcBuilderCustomizer();
    }

    private static final class SecurityMockMvcBuilderCustomizer implements MockMvcBuilderCustomizer {
        @Override
        public void customize(ConfigurableMockMvcBuilder<?> builder) {
            builder.apply(SecurityMockMvcConfigurers.springSecurity());
        }
    }
}
