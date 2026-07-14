package com.zsecurity.demo.repositories;


import com.zsecurity.demo.entity.OrderTracking;
import com.zsecurity.demo.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingRepo extends JpaRepository<OrderTracking,Integer> {

    List<OrderTracking> findByOrdersId(Orders orders);
}
