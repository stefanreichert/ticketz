package net.wickedshell.ticketz.adapter.jpa;

import net.wickedshell.ticketz.adapter.jpa.converter.ProjectToProjectEntityConverter;
import net.wickedshell.ticketz.adapter.jpa.converter.UserToUserEntityConverter;
import net.wickedshell.ticketz.adapter.jpa.entity.TicketEntity;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = PersistenceAdapterConfiguration.class)
@EntityScan(basePackageClasses = TicketEntity.class)
public class PersistenceAdapterConfiguration {

    @Bean
    public ModelMapper jpaModelMapper(UserToUserEntityConverter userConverter,
                                      ProjectToProjectEntityConverter projectConverter) {
        ModelMapper mapper = new ModelMapper();
        mapper.addConverter(userConverter);
        mapper.addConverter(projectConverter);
        return mapper;
    }
}
