package net.wickedshell.ticketz.adapter.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity for Project.
 * Maps to PROJECT_ENTITY table in the database.
 */
@Entity
@Table(name = "PROJECT_ENTITY")
@Data
public class ProjectEntity {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @NaturalId
    @Column(name = "code", nullable = false, length = 50)
    private String code;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "active", nullable = false)
    private boolean active;
    
    @Column(name = "date_created", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dateCreated;
    
    @Column(name = "date_updated", nullable = false)
    @UpdateTimestamp
    private LocalDateTime dateUpdated;
    
    @Version
    @Column(name = "version")
    private Long version;
}
