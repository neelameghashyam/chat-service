package com.zotly.zotlychatservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zotly.zotlychatservice.entity.AgentTakeover;

@Repository
public interface AgentTakeoverRepository extends JpaRepository<AgentTakeover, Long> {
}