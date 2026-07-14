package com.zsecurity.demo.repositories;
import com.zsecurity.demo.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FestivalRepo extends JpaRepository<Festival, Integer> {

    Optional<Festival> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate today1, LocalDate today2);
}