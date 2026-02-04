package net.wickedshell.ticketz.adapter.jpa.persistence;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.jpa.entity.UserEntity;
import net.wickedshell.ticketz.adapter.jpa.repository.UserRepository;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.driven.persistence.UserPersistence;
import net.wickedshell.ticketz.service.port.driven.persistence.exception.ObjectNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Component
@Validated
@RequiredArgsConstructor
public class UserJPAPersistenceImpl implements UserPersistence {

    @Qualifier("jpaModelMapper")
    private final ModelMapper mapper;
    private final UserRepository userRepository;

    @Override
    public User loadByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new ObjectNotFoundException("User not found: " + email));
        return mapper.map(userEntity, User.class);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Optional<UserEntity> userEntity = userRepository.findByEmail(email);
        if (userEntity.isPresent()) {
            return Optional.of(mapper.map(userEntity, User.class));
        }
        return Optional.empty();
    }

    @Override
    public User create(User user) {
        UserEntity userEntity = new UserEntity();
        mapper.map(user, userEntity);
        return mapper.map(userRepository.save(userEntity), User.class);
    }

    @Override
    public User update(User user) {
        UserEntity userEntityCurrent = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ObjectNotFoundException("User not found: " + user.getEmail()));
        mapper.map(user, userEntityCurrent);
        return mapper.map(userRepository.save(userEntityCurrent), User.class);
    }

    @Override
    public List<User> findAll() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false).map(userEntity -> mapper.map(userEntity, User.class)).toList();
    }
}
