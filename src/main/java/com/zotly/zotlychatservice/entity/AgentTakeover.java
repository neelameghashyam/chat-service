package com.zotly.zotlychatservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_takeovers")
public class AgentTakeover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long takeoverId;

    @Column(nullable = false)
    private Long conversationId;

    private Long fromAgent;

    @Column(nullable = false)
    private Long toAgent;

    private LocalDateTime takeoverTime = LocalDateTime.now();

    // ===== Getters and Setters =====
    public Long getTakeoverId() {
        return takeoverId;
    }

    public void setTakeoverId(Long takeoverId) {
        this.takeoverId = takeoverId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getFromAgent() {
        return fromAgent;
    }

    public void setFromAgent(Long fromAgent) {
        this.fromAgent = fromAgent;
    }

    public Long getToAgent() {
        return toAgent;
    }

    public void setToAgent(Long toAgent) {
        this.toAgent = toAgent;
    }

    public LocalDateTime getTakeoverTime() {
        return takeoverTime;
    }

    public void setTakeoverTime(LocalDateTime takeoverTime) {
        this.takeoverTime = takeoverTime;
    }
}