package com.zotly.zotlychatservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String threadId;
    private String message;
    private String chatToken;

    private Boolean archived;
    private Integer likeCount;
    private Integer dislikeCount;
    private Boolean isUnread;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(name = "customer_id") 
    private Long customerId;

    public enum Status {
        ACTIVE, CLOSED
    }
}