package com.zsecurity.demo.repositories;

import com.zsecurity.demo.entity.OrderItems;
import com.zsecurity.demo.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepo extends JpaRepository<OrderItems,Integer> {

    List<OrderItems> findByOrderId(Orders orders);


    @Query(value = """
    SELECT o.id, p.title
    FROM orders o
    JOIN order_items oi ON o.id = oi.order_id
    JOIN products p ON p.id = oi.prodid
    """, nativeQuery = true)
    List<Object[]> fetchOrderProducts();
}
