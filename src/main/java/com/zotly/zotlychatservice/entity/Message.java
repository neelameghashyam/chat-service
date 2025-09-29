package com.zotly.zotlychatservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    private Long conversationId;

    private String senderType;

    private Long senderId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String messageType;

    private LocalDateTime createdAt;
}
