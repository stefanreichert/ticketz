package net.wickedshell.ticketz.adapter.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@Entity
@Immutable
public class CommentEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String text;

    @ManyToOne(optional = false)
    private UserEntity author;

    @ManyToOne(optional = false)
    private TicketEntity ticket;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @PrePersist
    protected void onCreate() {
        this.dateCreated = LocalDateTime.now(ZoneOffset.UTC);
    }

}
