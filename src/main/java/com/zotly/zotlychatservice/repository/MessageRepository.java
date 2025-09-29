package com.zotly.zotlychatservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zotly.zotlychatservice.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}