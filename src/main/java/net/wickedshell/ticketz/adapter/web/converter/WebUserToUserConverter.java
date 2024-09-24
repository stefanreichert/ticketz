package net.wickedshell.ticketz.adapter.web.converter;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.web.model.WebUser;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.persistence.exception.ObjectNotFoundException;
import net.wickedshell.ticketz.service.port.rest.UserService;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebUserToUserConverter implements Converter<WebUser, User> {

    private final UserService userService;

    @Override
    public User convert(MappingContext<WebUser, User> mappingContext) {
        WebUser user = mappingContext.getSource();
        if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
            return this.userService.findByEmail(user.getEmail()).orElseThrow(ObjectNotFoundException::new);
        }
        return null;
    }
}
