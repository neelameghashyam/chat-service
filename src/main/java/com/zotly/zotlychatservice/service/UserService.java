package com.zotly.zotlychatservice.service;

import com.zotly.zotlychatservice.dto.UserDTO;
import com.zotly.zotlychatservice.entity.User;

public interface UserService {
    User signup(UserDTO userDTO);
    User login(UserDTO userDTO);
}