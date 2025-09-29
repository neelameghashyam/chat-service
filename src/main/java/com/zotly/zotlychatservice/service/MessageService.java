package com.zotly.zotlychatservice.service;

import java.util.List;

import com.zotly.zotlychatservice.dto.MessageDTO;

public interface MessageService {
    List<MessageDTO> getAll();
    MessageDTO getById(Long id);
    MessageDTO create(MessageDTO dto);
    MessageDTO update(Long id, MessageDTO dto);
    void delete(Long id);
}