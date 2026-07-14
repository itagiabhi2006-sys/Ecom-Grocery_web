package com.zsecurity.demo.repositories;

import com.zsecurity.demo.entity.UserProductInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InteractionRepo extends JpaRepository<UserProductInteraction, Long> {

    Optional<UserProductInteraction>
    findByUserIdAndProductId(Long userId, int productId);

    List<UserProductInteraction> findByUserId(Long userId);

    @Query("SELECT i FROM UserProductInteraction i ORDER BY i.viewCount DESC")
    List<UserProductInteraction> findTopViewed();

    List<UserProductInteraction> findByLastUpdatedBefore(LocalDateTime cutoff);
}