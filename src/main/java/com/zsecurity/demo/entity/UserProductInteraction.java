package com.zsecurity.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "user_product_interaction",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "productId"})
)
public class UserProductInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private int productId;

    private int searchCount;
    private int viewCount;
    private int clickCount;

    private LocalDateTime lastUpdated;
}
