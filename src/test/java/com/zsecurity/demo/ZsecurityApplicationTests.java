package com.zsecurity.demo;

import com.zsecurity.demo.entity.Orders;
import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.entity.Users;
import com.zsecurity.demo.repositories.OrderItemRepo;
import com.zsecurity.demo.repositories.OrderRepo;
import com.zsecurity.demo.repositories.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ZsecurityApplicationTests {



	@Autowired
	UserRepo userRepo;

	@Autowired
	OrderRepo orderRepo;

	@Autowired
	OrderItemRepo orderItemRepo;

	@Test
	void contextLoads(){
//
//		Users users = userRepo.findById(1l).orElseThrow();
//		List<Orders> orders = orderRepo.findByUserId(users);
//		System.out.println(orders.get(0).getTotalPrice());
//
//
//		Products products = Products.builder().id(1).title("rice").imageURL("hjk").build();
//		 Products products1 = Products.builder().id(2).title("wheat").imageURL("hjk").build();
//
//		Map<Products,Integer> mapLists = (Map.of(
//				products, 10,
//				products1, 20
//		));
//		ResponseOrderDetails details = ResponseOrderDetails.builder()
//				.timeOfOrder(LocalDateTime.now())
//				.totalPrice(1000.00)
//				.productQuantity(mapLists)
//				.build();
//
//		System.out.println(details.getTimeOfOrder());
//		for (Map.Entry<Products, Integer> entry : mapLists.entrySet()) {
//			System.out.println("Key: " + entry.getKey().getTitle() + ", Value: " + entry.getValue());
//		}

	}


}
