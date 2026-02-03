package net.wickedshell.ticketz.adapter.jpa;

import net.wickedshell.ticketz.adapter.jpa.converter.ProjectToProjectEntityConverter;
import net.wickedshell.ticketz.adapter.jpa.converter.UserToUserEntityConverter;
import net.wickedshell.ticketz.adapter.jpa.entity.CommentEntity;
import net.wickedshell.ticketz.adapter.jpa.entity.TicketEntity;
import net.wickedshell.ticketz.service.model.Comment;
import net.wickedshell.ticketz.service.model.Ticket;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
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
        mapper.getConfiguration().setImplicitMappingEnabled(false);

        TypeMap<Ticket, TicketEntity> ticketTypeMap = mapper.createTypeMap(Ticket.class, TicketEntity.class);
        ticketTypeMap.addMappings(m -> {
            m.using(userConverter).map(Ticket::getAuthor, TicketEntity::setAuthor);
            m.using(userConverter).map(Ticket::getEditor, TicketEntity::setEditor);
            m.using(projectConverter).map(Ticket::getProject, TicketEntity::setProject);
        });
        ticketTypeMap.implicitMappings();

        TypeMap<Comment, CommentEntity> commentTypeMap = mapper.createTypeMap(Comment.class, CommentEntity.class);
        commentTypeMap.addMappings(m -> m.using(userConverter).map(Comment::getAuthor, CommentEntity::setAuthor));
        commentTypeMap.implicitMappings();

        mapper.getConfiguration().setImplicitMappingEnabled(true);
        return mapper;
    }
}
