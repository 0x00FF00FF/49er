package org.rares.miner49er.network.dto.converter;

import io.reactivex.Single;
import lombok.Builder;
import org.rares.miner49er.network.dto.UserDto;
import org.rares.miner49er.persistence.entities.User;

@Builder
public class UserConverter {
    public static User toModelBlocking(UserDto userDto){
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPhoto(userDto.getPicture());
        user.setRole(userDto.getRole().value);
        user.setObjectId(userDto.getId());
        return user;
    }

    public static Single<User> toModel(UserDto userDto){
        return Single.just(userDto)
                .map(ud->{
                    User user = new User();
                    user.setName(userDto.getName());
                    user.setEmail(userDto.getEmail());
                    user.setPhoto(userDto.getPicture());
                    user.setRole(userDto.getRole().value);
                    user.setObjectId(userDto.getId());
                    return user;
                });
    }
}
