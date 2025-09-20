package com.zotly.zotlychatservice.service.impl;

import com.zotly.zotlychatservice.dto.UserDTO;
import com.zotly.zotlychatservice.entity.User;
import com.zotly.zotlychatservice.repository.UserRepository;
import com.zotly.zotlychatservice.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User signup(UserDTO userDTO) {
        if (userRepository.findByUsername(userDTO.username()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(userDTO.username());
        user.setPassword(userDTO.password()); // Plain password as per requirement
        return userRepository.save(user);
    }

    @Override
    public User login(UserDTO userDTO) {
        User user = userRepository.findByUsername(userDTO.username())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        if (!user.getPassword().equals(userDTO.password())) {
            throw new RuntimeException("Invalid username or password");
        }
        return user;
    }
}