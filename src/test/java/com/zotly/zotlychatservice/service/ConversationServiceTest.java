package com.zotly.zotlychatservice.service;

import com.zotly.zotlychatservice.entity.Conversation;
import com.zotly.zotlychatservice.repository.ConversationRepository;
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
class ConversationServiceTest {

    @Mock
    private ConversationRepository repository;

    @InjectMocks
    private ConversationService service;

    @Test
    void save_success() {
        Conversation conv = Conversation.builder().build();
        Conversation saved = Conversation.builder().id(1L).build();

        when(repository.save(any(Conversation.class))).thenReturn(saved);

        Conversation result = service.save(conv);

        assertEquals(1L, result.getId());
        verify(repository).save(conv);
    }

    @Test
    void getAll_success() {
        Conversation c1 = Conversation.builder().id(1L).build();
        Conversation c2 = Conversation.builder().id(2L).build();
        List<Conversation> list = Arrays.asList(c1, c2);

        when(repository.findAll()).thenReturn(list);

        List<Conversation> result = service.getAll();

        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    @Test
    void getById_success() {
        Conversation conv = Conversation.builder().id(1L).build();

        when(repository.findById(1L)).thenReturn(Optional.of(conv));

        Optional<Conversation> result = service.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(repository).findById(1L);
    }

    @Test
    void getById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<Conversation> result = service.getById(1L);

        assertFalse(result.isPresent());
        verify(repository).findById(1L);
    }

    @Test
    void getByStatus_success() {
        Conversation c1 = Conversation.builder().id(1L).build();
        List<Conversation> list = Arrays.asList(c1);

        when(repository.findByStatus("ACTIVE")).thenReturn(list);

        List<Conversation> result = service.getByStatus("ACTIVE");

        assertEquals(1, result.size());
        verify(repository).findByStatus("ACTIVE");
    }

    @Test
    void getByCustomerId_success() {
        Conversation c1 = Conversation.builder().id(1L).build();
        List<Conversation> list = Arrays.asList(c1);

        when(repository.findByCustomerId(1L)).thenReturn(list);

        List<Conversation> result = service.getByCustomerId(1L);

        assertEquals(1, result.size());
        verify(repository).findByCustomerId(1L);
    }
}