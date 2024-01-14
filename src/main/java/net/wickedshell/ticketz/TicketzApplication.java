package net.wickedshell.ticketz;

import net.wickedshell.ticketz.adapter.jpa.PersistenceAdapterConfiguration;
import net.wickedshell.ticketz.adapter.rest.RestAdapterConfiguration;
import net.wickedshell.ticketz.service.ServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@SpringBootApplication
@EnableMethodSecurity(prePostEnabled = true)
@Import({PersistenceAdapterConfiguration.class, RestAdapterConfiguration.class, ServiceConfiguration.class})
public class TicketzApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketzApplication.class, args);
    }

    @Bean
    public MethodValidationPostProcessor validationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

}
