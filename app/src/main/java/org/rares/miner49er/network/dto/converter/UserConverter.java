package org.rares.miner49er.network.dto.converter;

import org.rares.miner49er.network.dto.UserDto;
import org.rares.miner49er.persistence.entities.User;

public class UserConverter {
    User toModel(UserDto userDto){
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPhoto(userDto.getPicture());
        user.setRole(userDto.getRole().value);
        user.setObjectId(userDto.getId());
        return user;
    }
}
