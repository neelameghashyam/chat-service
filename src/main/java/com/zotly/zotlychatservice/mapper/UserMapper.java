package com.zotly.zotlychatservice.mapper;

import com.zotly.zotlychatservice.dto.UserDTO;
import com.zotly.zotlychatservice.entity.User;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        return new UserDTO(user.getUsername(), user.getPassword());
    }

    public static User toEntity(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.username());
        user.setPassword(userDTO.password());
        return user;
    }
}