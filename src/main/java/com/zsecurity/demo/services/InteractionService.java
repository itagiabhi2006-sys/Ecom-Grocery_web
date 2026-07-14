package com.zsecurity.demo.services;

import com.zsecurity.demo.entity.UserProductInteraction;
import com.zsecurity.demo.repositories.InteractionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class InteractionService {

    @Autowired
    private InteractionRepo repo;

    public void track(Long userId, int productId, String type) {

        if (userId == null || productId == 0) {
            System.out.println("Invalid data: " + userId + " " + productId);
            return;
        }

        UserProductInteraction interaction =
                repo.findByUserIdAndProductId(userId, productId)
                        .orElseGet(() -> {
                            UserProductInteraction i = new UserProductInteraction();
                            i.setUserId(userId);
                            i.setProductId(productId);
                            i.setSearchCount(0);
                            i.setViewCount(0);
                            i.setClickCount(0);
                            return i;
                        });

        //  Prevent duplicate within 10 minutes
        if (interaction.getLastUpdated() != null &&
                interaction.getLastUpdated().isAfter(LocalDateTime.now().minusMinutes(10))) {
            return;
        }

        //  Update counts
        switch (type) {
            case "VIEW":
                interaction.setViewCount(interaction.getViewCount() + 1);
                break;

            case "CLICK":
                interaction.setClickCount(interaction.getClickCount() + 1);
                break;
        }

        //  Update timestamp
        interaction.setLastUpdated(LocalDateTime.now());

        repo.save(interaction);
    }

    public void deleteLeastSeenInteractions() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);

        //  1. Get all interactions not updated in last 3 days
        List<UserProductInteraction> stale = repo
                .findByLastUpdatedBefore(cutoff);

        if (stale.isEmpty()) return;

        //  2. Find the least seen threshold (bottom 20% by score)
        List<UserProductInteraction> scored = stale.stream()
                .sorted(Comparator.comparingInt(i ->
                        (i.getViewCount() * 2) + i.getClickCount() + i.getSearchCount()))
                .toList();

        int cutoffIndex = (int) Math.ceil(scored.size() * 0.20);
        List<UserProductInteraction> toDelete = scored.subList(0, cutoffIndex);

        repo.deleteAll(toDelete);
    }
}
