package net.wickedshell.ticketz.adapter.jpa.converter;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.jpa.entity.UserEntity;
import net.wickedshell.ticketz.adapter.jpa.repository.UserRepository;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserToUserEntityConverter implements Converter<User, UserEntity> {

    private final UserRepository userRepository;

    @Override
    public UserEntity convert(MappingContext<User, UserEntity> mappingContext) {
        User user = mappingContext.getSource();
        if (user != null) {
            return this.userRepository.findByEmail(user.getEmail()).orElseThrow(ObjectNotFoundException::new);
        }
        return null;
    }
}
