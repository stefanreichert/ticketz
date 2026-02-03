package net.wickedshell.ticketz.adapter.web.converter;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.model.UserWeb;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException;
import net.wickedshell.ticketz.service.port.access.UserService;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebUserToUserConverter implements Converter<UserWeb, User> {

    private final UserService userService;

    @Override
    public User convert(MappingContext<UserWeb, User> mappingContext) {
        UserWeb user = mappingContext.getSource();
        if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
            return this.userService.findByEmail(user.getEmail()).orElseThrow(ObjectNotFoundException::new);
        }
        return null;
    }
}
