package net.wickedshell.ticketz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class TicketZApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketZApplication.class, args);
    }
}
