package com.zsecurity.demo.repositories;

import com.zsecurity.demo.entity.Orders;
import com.zsecurity.demo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Orders,Integer> {

    List<Orders> findByUserId(Users userId);

    // 🔥 Trending Categories
    @Query(value = """
        SELECT c.id, c.name, COUNT(oi.id) AS total_sales
        FROM order_items oi
        JOIN products p ON oi.prodid = p.id
        JOIN categories c ON p.category_id = c.id
        GROUP BY c.id, c.name
        ORDER BY total_sales DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTrendingCategories(@Param("limit") int limit);


    // 🔥 Trending Products
    @Query(value = """
        SELECT p.id, p.title, SUM(oi.quantity) AS total_sold
        FROM order_items oi
        JOIN products p ON oi.prodid = p.id
        GROUP BY p.id, p.title
        ORDER BY total_sold DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTrendingProducts(@Param("limit") int limit);


    // 🔥 Most Bought (by frequency)
    @Query(value = """
        SELECT p.id, p.title, COUNT(oi.id) AS times_bought
        FROM order_items oi
        JOIN products p ON oi.prodid = p.id
        GROUP BY p.id, p.title
        ORDER BY times_bought DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findMostBought(@Param("limit") int limit);


    // 🔥 Buy Again (User Specific)
    @Query(value = """
        SELECT p.id, p.title, COUNT(oi.id) AS times_bought
        FROM orders o
        JOIN order_items oi ON o.id = oi.order_id
        JOIN products p ON oi.prodid = p.id
        WHERE o.user_id = :userId
        GROUP BY p.id, p.title
        ORDER BY times_bought DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findBuyAgain(
            @Param("userId") Long userId,
            @Param("limit") int limit);


    @Query(value = """
        SELECT o.id, p.title
        FROM orders o
        JOIN order_items oi ON o.id = oi.order_id
        JOIN products p ON oi.prodid = p.id
        """, nativeQuery = true)

    List<Object[]> fetchOrderProducts();


    @Query("SELECT o FROM Orders o " +
            "JOIN FETCH o.items i " +
            "JOIN FETCH i.prodId " +
            "WHERE o.userId = :user")
    List<Orders> findOrdersWithItems(@Param("user") Users user);



    @Query("SELECT o FROM Orders o JOIN FETCH o.userId u JOIN FETCH o.userAddressDetails a")
    List<Orders> findAllLite();

    @Query("SELECT DISTINCT o FROM Orders o " +
            "JOIN FETCH o.userId u " +
            "JOIN FETCH o.userAddressDetails a " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.prodId p " +
            "WHERE o.id = :orderId")
    Orders findOrderFullDetails(@Param("orderId") int orderId);

}
