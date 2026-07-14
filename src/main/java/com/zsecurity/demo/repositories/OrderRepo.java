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
import org.springframework.data.domain.Pageable;

@Repository
public interface OrderRepo extends JpaRepository<Orders,Integer> {

    List<Orders> findByUserId(Users userId);

    // 🔥 Trending Categories
    @Query("SELECT new com.zsecurity.demo.dtos.AnalyticsCategoryDTO(c.id, c.name, c.imageURL, COUNT(oi.id)) " +
           "FROM OrderItems oi JOIN oi.prodId p JOIN p.categories c " +
           "GROUP BY c.id, c.name, c.imageURL " +
           "ORDER BY COUNT(oi.id) DESC")
    List<com.zsecurity.demo.dtos.AnalyticsCategoryDTO> findTrendingCategories(Pageable pageable);


    // 🔥 Trending Products
    @Query("SELECT new com.zsecurity.demo.dtos.AnalyticsResponseDTO(p.id, p.title, p.description, p.price, p.imageURL, p.stock, SUM(oi.quantity)) " +
           "FROM OrderItems oi JOIN oi.prodId p " +
           "GROUP BY p.id, p.title, p.description, p.price, p.imageURL, p.stock " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<com.zsecurity.demo.dtos.AnalyticsResponseDTO> findTrendingProducts(Pageable pageable);


    // 🔥 Most Bought (by frequency)
    @Query("SELECT new com.zsecurity.demo.dtos.AnalyticsResponseDTO(p.id, p.title, p.description, p.price, p.imageURL, p.stock, COUNT(oi.id)) " +
           "FROM OrderItems oi JOIN oi.prodId p " +
           "GROUP BY p.id, p.title, p.description, p.price, p.imageURL, p.stock " +
           "ORDER BY COUNT(oi.id) DESC")
    List<com.zsecurity.demo.dtos.AnalyticsResponseDTO> findMostBought(Pageable pageable);


    // 🔥 Buy Again (User Specific)
    @Query("SELECT new com.zsecurity.demo.dtos.AnalyticsResponseDTO(p.id, p.title, p.description, p.price, p.imageURL, p.stock, COUNT(oi.id)) " +
           "FROM Orders o JOIN o.items oi JOIN oi.prodId p " +
           "WHERE o.userId.id = :userId " +
           "GROUP BY p.id, p.title, p.description, p.price, p.imageURL, p.stock " +
           "ORDER BY COUNT(oi.id) DESC")
    List<com.zsecurity.demo.dtos.AnalyticsResponseDTO> findBuyAgain(
            @Param("userId") Long userId,
            Pageable pageable);


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
