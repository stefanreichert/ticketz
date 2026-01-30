package net.wickedshell.ticketz.adapter.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
public class TicketEntity {

    @Id
    @GeneratedValue
    private Long id;

    @NaturalId
    @Column(nullable = false)
    private String ticketNumber;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false)
    private String description;

    @ManyToOne(optional = false)
    private UserEntity author;

    @ManyToOne
    private UserEntity editor;

    @Column(nullable = false)
    private TicketState state;

    @ManyToOne(optional = false)
    private ProjectEntity project;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dateCreated;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime dateUpdated;

    @Version
    private long version;
}
