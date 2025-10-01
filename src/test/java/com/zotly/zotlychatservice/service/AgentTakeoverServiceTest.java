package com.zotly.zotlychatservice.service;

import com.zotly.zotlychatservice.entity.AgentTakeover;
import com.zotly.zotlychatservice.repository.AgentTakeoverRepository;
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
class AgentTakeoverServiceTest {

    @Mock
    private AgentTakeoverRepository repository;

    @InjectMocks
    private AgentTakeoverService service;

    @Test
    void save_success() {
        AgentTakeover takeover = new AgentTakeover();
        takeover.setConversationId(1L);
        AgentTakeover saved = new AgentTakeover();
        saved.setTakeoverId(1L);

        when(repository.save(any(AgentTakeover.class))).thenReturn(saved);

        AgentTakeover result = service.save(takeover);

        assertNotNull(result);
        assertEquals(1L, result.getTakeoverId());
        verify(repository).save(takeover);
    }

    @Test
    void findAll_success() {
        AgentTakeover t1 = new AgentTakeover();
        AgentTakeover t2 = new AgentTakeover();
        List<AgentTakeover> list = Arrays.asList(t1, t2);

        when(repository.findAll()).thenReturn(list);

        List<AgentTakeover> result = service.findAll();

        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    @Test
    void findById_success() {
        AgentTakeover takeover = new AgentTakeover();
        takeover.setTakeoverId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(takeover));

        Optional<AgentTakeover> result = service.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getTakeoverId());
        verify(repository).findById(1L);
    }

    @Test
    void findById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<AgentTakeover> result = service.findById(1L);

        assertFalse(result.isPresent());
        verify(repository).findById(1L);
    }

    @Test
    void update_success() {
        AgentTakeover existing = new AgentTakeover();
        existing.setTakeoverId(1L);
        existing.setConversationId(1L);
        AgentTakeover updated = new AgentTakeover();
        updated.setConversationId(2L);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(AgentTakeover.class))).thenReturn(existing);

        AgentTakeover result = service.update(1L, updated);

        assertEquals(2L, result.getConversationId());
        verify(repository).findById(1L);
        verify(repository).save(existing);
    }

    @Test
    void update_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.update(1L, new AgentTakeover()));

        assertEquals("AgentTakeover not found with id 1", exception.getMessage());
        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void delete_success() {
        doNothing().when(repository).deleteById(1L);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }
}