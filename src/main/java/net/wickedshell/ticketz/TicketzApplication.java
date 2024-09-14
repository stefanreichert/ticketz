package net.wickedshell.ticketz;

import net.wickedshell.ticketz.adapter.AuthenticationConfiguration;
import net.wickedshell.ticketz.adapter.jpa.PersistenceAdapterConfiguration;
import net.wickedshell.ticketz.adapter.rest.RestAdapterConfiguration;
import net.wickedshell.ticketz.adapter.web.WebAdapterConfiguration;
import net.wickedshell.ticketz.service.ServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
@Import({AuthenticationConfiguration.class, WebAdapterConfiguration.class, PersistenceAdapterConfiguration.class, RestAdapterConfiguration.class, ServiceConfiguration.class})
public class TicketzApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketzApplication.class, args);
    }
}
