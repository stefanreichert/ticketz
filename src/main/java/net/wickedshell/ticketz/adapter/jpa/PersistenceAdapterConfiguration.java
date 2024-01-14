package net.wickedshell.ticketz.adapter.jpa;

import net.wickedshell.ticketz.adapter.jpa.entity.TicketEntity;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = PersistenceAdapterConfiguration.class)
@EntityScan(basePackageClasses = TicketEntity.class)
public class PersistenceAdapterConfiguration {

}
