package com.zotly.zotlychatservice.service.impl;

import com.zotly.zotlychatservice.dto.MessageDTO;
import com.zotly.zotlychatservice.entity.Message;
import com.zotly.zotlychatservice.mapper.MessageMapper;
import com.zotly.zotlychatservice.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository repository;

    @InjectMocks
    private MessageServiceImpl service;

    @Test
    void getAll_success() {
        Message m1 = Message.builder().messageId(1L).build();
        Message m2 = Message.builder().messageId(2L).build();
        when(repository.findAll()).thenReturn(Arrays.asList(m1, m2));

        List<MessageDTO> result = service.getAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).messageId());
        verify(repository).findAll();
    }

    @Test
    void getById_success() {
        Message message = Message.builder().messageId(1L).conversationId(1L).senderType("USER").senderId(1L).content("Hello").messageType("TEXT").build();
        when(repository.findById(1L)).thenReturn(Optional.of(message));

        MessageDTO result = service.getById(1L);

        assertEquals(1L, result.messageId());
        assertEquals("Hello", result.content());
        verify(repository).findById(1L);
    }

    @Test
    void getById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.getById(1L));

        assertEquals("Message not found with id 1", exception.getMessage());
        verify(repository).findById(1L);
    }

    @Test
    void create_success() {
        MessageDTO dto = new MessageDTO(null, 1L, "USER", 1L, "Hello", "TEXT");
        Message entity = MessageMapper.toEntity(dto);
        Message saved = Message.builder().messageId(1L).conversationId(1L).senderType("USER").senderId(1L).content("Hello").messageType("TEXT").build();
        when(repository.save(any(Message.class))).thenReturn(saved);

        MessageDTO result = service.create(dto);

        assertEquals(1L, result.messageId());
        verify(repository).save(any(Message.class));
    }

    @Test
    void update_success() {
        MessageDTO dto = new MessageDTO(1L, 1L, "USER", 1L, "Updated", "TEXT");
        Message existing = Message.builder().messageId(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Message.class))).thenReturn(existing);

        MessageDTO result = service.update(1L, dto);

        assertEquals(1L, result.messageId());
        verify(repository).findById(1L);
        verify(repository).save(existing);
    }

    @Test
    void update_notFound() {
        MessageDTO dto = new MessageDTO(1L, 1L, "USER", 1L, "Updated", "TEXT");
        when(repository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.update(1L, dto));

        assertEquals("Message not found with id 1", exception.getMessage());
        verify(repository).findById(1L);
    }

    @Test
    void delete_success() {
        doNothing().when(repository).deleteById(1L);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }
}