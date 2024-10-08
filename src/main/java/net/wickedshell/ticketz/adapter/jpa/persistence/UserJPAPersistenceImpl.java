package net.wickedshell.ticketz.adapter.jpa.persistence;

import lombok.RequiredArgsConstructor;
import net.wickedshell.ticketz.adapter.jpa.entity.UserEntity;
import net.wickedshell.ticketz.adapter.jpa.repository.UserRepository;
import net.wickedshell.ticketz.service.model.User;
import net.wickedshell.ticketz.service.port.persistence.UserPersistence;
import net.wickedshell.ticketz.service.port.persistence.exception.ObjectNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class UserJPAPersistenceImpl implements UserPersistence {

    private final ModelMapper mapper = new ModelMapper();
    private final UserRepository userRepository;

    @Override
    public User loadByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(ObjectNotFoundException::new);
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
        UserEntity userEntity = mapper.map(user, UserEntity.class);
        return mapper.map(userRepository.save(userEntity), User.class);
    }

    @Override
    public User update(User user) {
        UserEntity userEntityCurrent = userRepository.findByEmail(user.getEmail()).orElseThrow(ObjectNotFoundException::new);
        mapper.map(user, userEntityCurrent);
        return mapper.map(userRepository.save(userEntityCurrent), User.class);
    }

    @Override
    public List<User> findAll() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false).map(userEntity -> mapper.map(userEntity, User.class)).toList();
    }
}
