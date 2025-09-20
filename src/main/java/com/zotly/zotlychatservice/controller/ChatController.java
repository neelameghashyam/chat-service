package com.zotly.zotlychatservice.controller;

import com.zotly.zotlychatservice.dto.ConversationDTO;
import com.zotly.zotlychatservice.dto.UserDTO;
import com.zotly.zotlychatservice.entity.Conversation;
import com.zotly.zotlychatservice.entity.User;
import com.zotly.zotlychatservice.mapper.ConversationMapper;
import com.zotly.zotlychatservice.mapper.UserMapper;
import com.zotly.zotlychatservice.service.ConversationService;
import com.zotly.zotlychatservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final UserService userService;
    private final ConversationService conversationService;

    public ChatController(UserService userService, ConversationService conversationService) {
        this.userService = userService;
        this.conversationService = conversationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody UserDTO userDTO) {
        User user = userService.signup(userDTO);
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody UserDTO userDTO) {
        User user = userService.login(userDTO);
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationDTO> createConversation(@RequestBody ConversationDTO conversationDTO) {
        Conversation conversation = conversationService.createConversation(conversationDTO);
        return ResponseEntity.ok(ConversationMapper.toDTO(conversation));
    }
}