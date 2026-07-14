package com.zsecurity.demo.services;

import com.zsecurity.demo.dtos.CategoryDto;
import com.zsecurity.demo.dtos.ProductQuantityResponse;
import com.zsecurity.demo.dtos.RefundRequest;
import com.zsecurity.demo.dtos.ResponseOrderDetails;
import com.zsecurity.demo.entity.*;
import com.zsecurity.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service

public class ProdServices   {

    @Autowired
    ProdRepo prodRepo;

    @Autowired
    CartRepository cartRepo;

    @Autowired
    CateRepo cateRepo;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    OrderItemRepo orderItemRepo;

    @Autowired
    TrackingRepo trackingRepo;

    @Autowired
    UserAddressRepo userAddressRepo;

    @Autowired
    SavedAdressRepo savedAdressRepo;

    @Autowired
    EmailService emailService;

    @Autowired
    SpecialService specialService;




    @Cacheable(value = "allProducts", sync = true)
    public List<Products> getAllProducts() {
        return prodRepo.findAll();
    }


    @Cacheable(value = "allCategories", sync = true)
    public List<CategoryDto> getAllCategories() {
        return cateRepo.findAll().stream()
                .map(c -> CategoryDto.builder()
                        .id( c.getId())
                        .name(c.getName())
                        .imageURL(c.getImageURL())
                        .productCount(prodRepo.countByCategories_Id(c.getId()))
                        .build()
                )
                .toList();
    }


    public Products getProductById(int id) {
        return prodRepo.findById(id)
                .orElseThrow(()->new RuntimeException("Product not found"));
    }

    @Transactional
    public String createOrder(RefundRequest.OrderFullDetails request) {

        // ✅ 1. User
        Users user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ 2. Address
        SavedAddress savedAddress = savedAdressRepo.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        UserAddressDetails address = UserAddressDetails.builder()
                .address(savedAddress.getAddress())
                .city(savedAddress.getCity())
                .phone(savedAddress.getPhone())
                .pincode(savedAddress.getPincode())
                .fullName(savedAddress.getFullName())
                .build();

        userAddressRepo.save(address);

        // ✅ 3. Prepare Items
        List<RefundRequest.OrderItemRequest> items;
        List<Cart> cartList = null;

        if (request.getItems() == null || request.getItems().isEmpty()) {

            // 🛒 CART
            cartList = cartRepo.findCartByUserId(request.getUserId());

            if (cartList.isEmpty()) throw new RuntimeException("Cart is empty");

            items = cartList.stream().map(c -> {
                RefundRequest.OrderItemRequest i = new RefundRequest.OrderItemRequest();
                i.setProductId(c.getProducts().getId());
                i.setQuantity(c.getQuantity());
                return i;
            }).toList();

        } else {
            // ⚡ BUY NOW
            items = request.getItems();
        }

        // ✅ 4. Validate + Calculate Total
        double total = 0.0;

        for (RefundRequest.OrderItemRequest item : items) {
            Products product = prodRepo.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + product.getTitle());
            }

            total += product.getPrice() * item.getQuantity();
        }

        // ✅ 5. APPLY OFFER (FINAL LOGIC)
        int offerAmount = 0;

        if (request.isApplyOffer()) {
            offerAmount = specialService.getOfferAmount(user.getId());
            if (offerAmount > total) {
                offerAmount = (int) total;
            }

            total = total - offerAmount;
        }

        // ✅ 6. Payment
        String paymentStatus = "PENDING";
        String razorpayOrderId = null;
        String razorpayPaymentId = null;
        String razorpaySignature = null;

        if ("ONLINE".equals(request.getPaymentMethod())) {

            if (request.getPaymentDetails() == null) {
                throw new RuntimeException("Payment details missing");
            }

            Map<String, String> details = request.getPaymentDetails();

            razorpayOrderId = details.get("razorpay_order_id");
            razorpayPaymentId = details.get("razorpay_payment_id");
            razorpaySignature = details.get("razorpay_signature");

            paymentStatus = "COMPLETED";
        }

        // ✅ 7. Save Order
        Orders order = Orders.builder()
                .status("Ordered")
                .userId(user)
                .totalPrice(total)
                .discountAmount(offerAmount) // ✅ SAVE DISCOUNT
                .userAddressDetails(address)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(paymentStatus)
                .razorpayOrderId(razorpayOrderId)
                .razorpayPaymentId(razorpayPaymentId)
                .razorpaySignature(razorpaySignature)
                .build();

        orderRepo.save(order);

        // ✅ 8. Save Items + Update Stock
        for (RefundRequest.OrderItemRequest item : items) {

            Products product = prodRepo.findById(item.getProductId()).get();

            OrderItems orderItem = OrderItems.builder()
                    .prodId(product)
                    .quantity(item.getQuantity())
                    .price(product.getPrice() * item.getQuantity())
                    .orderId(order)
                    .build();

            orderItemRepo.save(orderItem);

            product.setStock(product.getStock() - item.getQuantity());
            prodRepo.save(product);
        }

        // ✅ 9. Tracking
        trackingRepo.save(OrderTracking.builder()
                .ordersId(order)
                .stage("Ordered")
                .updatedBy("USER")
                .build());


        // ✅ 10. Send Order Confirmation Email
       // emailService.generateMailForCreatingOrder(user, order, order.getItems());


        // ✅ 10. Clear Cart
        if (cartList != null) {
            cartRepo.deleteAll(cartList);
        }

        return "Order placed successfully! ID: " + order.getId();
    }



    public Products addProduct(Products products) {
        return prodRepo.save(products);
    }


    public Categories addCategories(Categories categories) {
        return cateRepo.save(categories);
    }


    public Users addUser(Users users) {
        return userRepo.save(users);
    }

    public Products assignCategoryToProduct(int pid, int cid) {
        Categories categories = cateRepo.findById(cid).orElseThrow(()->new RuntimeException("Category with id not found"));
        Products products = prodRepo.findById(pid).orElseThrow(()->new RuntimeException("Product with id not found"));
        products.setCategories(categories);
        return prodRepo.save(products);
    }

    public Orders placeOrder(long userId, int orderId) {
        Users users = userRepo.findById(userId).orElseThrow(()->new RuntimeException("User with id not found"));
        Orders orders = orderRepo.findById(orderId).orElseThrow(()->new RuntimeException("Order with id not found"));
        orders.setUserId(users);
        return orderRepo.save(orders);
    }

    public List<Products> getAllProductsBelongsTOcategory(int id) {
        Categories categories = cateRepo.findById(id).orElseThrow();
        return categories.getProducts();
    }

    public List<Products> searchProducts(String q, Double minPrice, Double maxPrice, String sort) {
        List<Products> products = List.of();

        if (q != null && !q.isEmpty()) {
            products = prodRepo.findAll();
            String lowerQ = q.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getTitle().toLowerCase().contains(lowerQ))
                    .toList();
        }

        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() >= minPrice)
                    .toList();
        }
        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() <= maxPrice)
                    .toList();
        }

        if (sort != null) {
            switch (sort) {
                case "price_asc":
                    products = products.stream()
                            .sorted(Comparator.comparing(Products::getPrice))
                            .toList();
                    break;
                case "price_desc":
                    products = products.stream()
                            .sorted(Comparator.comparing(Products::getPrice).reversed())
                            .toList();
                    break;
                default:

            }
        }

        return products;
    }


    public List<ResponseOrderDetails> getOrderDetailsByUserId(long userId) {

        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Orders> ordersList = orderRepo.findOrdersWithItems(user);

        List<ResponseOrderDetails> responseOrderDetails = new ArrayList<>();

        for (Orders order : ordersList) {

            List<ProductQuantityResponse> productList = order.getItems()
                    .stream()
                    .map(item -> {
                        Products product = item.getProdId();
                        return ProductQuantityResponse.builder()
                                .productId(product.getId())
                                .productName(product.getTitle())
                                .productImage(product.getImageURL())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build();
                    })
                    .toList();

            ResponseOrderDetails orderDetails = ResponseOrderDetails.builder()
                    .orderId(order.getId())
                    .timeOfOrder(order.getOrderedAt())
                    .totalPrice(order.getTotalPrice())
                    .productList(productList)
                    .status(order.getStatus())
                    .paymentMode(order.getPaymentMethod())
                    .refundStatus(order.getRefundStatus())
                    .refundUpi(order.getRefundUpi())
                    .bankAccountNumber(order.getBankAccountNumber())
                    .discountAmount(order.getDiscountAmount())
                    .build();

            responseOrderDetails.add(orderDetails);
        }

        return responseOrderDetails;
    }

    public void cancelOrder(int orderId) {
        Orders orders = orderRepo.findById(orderId).orElseThrow();
        orders.setStatus("Canceled");
        List<OrderTracking> orderTrackings = trackingRepo.findByOrdersId(orders);
        for(OrderTracking orderTracking : orderTrackings){
            List<OrderItems> orderItems = orderTracking.getOrdersId().getItems();
            for(OrderItems orderItem : orderItems ){
               Products products = orderItem.getProdId();
               products.setStock(orderItem.getProdId().getStock() + orderItem.getQuantity());
                prodRepo.save(products);
            }
        }
        orderRepo.save(orders);

    }


    public List<ResponseOrderDetails.TrackDto> getOrderStatusTracking(int orderID) {
        Orders orders = orderRepo.findById(orderID).orElseThrow();
        List<OrderTracking> orderTrackings=  trackingRepo.findByOrdersId(orders);

        List<ResponseOrderDetails.TrackDto> trackDtos = new ArrayList<>();
        for(OrderTracking orderTracking : orderTrackings){
            ResponseOrderDetails.TrackDto trackDto = ResponseOrderDetails.TrackDto.builder().orderId(orderTracking.getOrdersId().getId()).Stage(orderTracking.getStage())
                    .updatedDate(orderTracking.getUpdatedTime())
                    .description(orderTracking.getDescription())
                    .build();
            trackDtos.add(trackDto);
        }

        return trackDtos;
    }


    public List<Cart> getCartItems(int userId) {
        Users user = userRepo.findById((long) userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cartRepo.findByUserAndIsOrderedFalse(user);
    }


    public void addItemToCart(List<Cart> cartList, int userId) {
        Users user = userRepo.findById((long) userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Cart incoming : cartList) {

            Cart existing = cartRepo.findByUserIdAndProductsId(userId, incoming.getProducts().getId());

            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + incoming.getQuantity());
                cartRepo.save(existing);
            } else {
                incoming.setUser(user);
                incoming.setOrdered(false);
                cartRepo.save(incoming);
            }
        }
    }


    public void removeFromCart(int cartID) {
        cartRepo.deleteById(cartID);
    }

    public void updateCart(int cartId, int quantity) {
        Cart cart = cartRepo.findById(cartId).orElseThrow();
        cart.setQuantity(quantity);
        cart.setPrices(quantity * cart.getProducts().getPrice());
        cartRepo.save(cart);

    }

    public List<SavedAddress> getUserAddressSaved(int userId) {
        return savedAdressRepo.findByUserId(userId);

    }

    public void deleteSavedAddress(int addressId) {
        savedAdressRepo.deleteById(addressId);
    }

    public SavedAddress updateSavedAddress(SavedAddress userAddressDetails, int addressId) {
        SavedAddress userAddressDetails1 = savedAdressRepo.findById(addressId).orElseThrow();
        if(userAddressDetails1.getAddress() != null){
            userAddressDetails1.setAddress(userAddressDetails.getAddress());
        }
        if(userAddressDetails1.getCity() != null){
            userAddressDetails1.setCity(userAddressDetails.getCity());
        }
        if(userAddressDetails1.getPhone() != 0){
            userAddressDetails1.setPhone(userAddressDetails.getPhone());
        }
        if(userAddressDetails1.getPincode() != 0){
            userAddressDetails1.setPincode(userAddressDetails.getPincode());
        }
        if(userAddressDetails1.getFullName() != null){
            userAddressDetails1.setFullName(userAddressDetails.getFullName());
        }

        return savedAdressRepo.save(userAddressDetails1);
    }

    public SavedAddress addAddress(SavedAddress savedAddress) {
        return savedAdressRepo.save(savedAddress);
    }

    public UserAddressDetails orderedUserDetails(int orderId) {
        Orders orders  = orderRepo.findById(orderId).orElseThrow();
        return orders.getUserAddressDetails();
    }


    public void updateTrackRecord(int orderId,String desc) {
        Orders orders = orderRepo.findById(orderId).orElseThrow();
        OrderTracking orderTracking = trackingRepo.findByOrdersId(orders).getFirst();
        OrderTracking orderTracking1 = OrderTracking.builder().updatedBy("USER").updatedTime(LocalDateTime.now())
                .ordersId(orderRepo.findById(orderId).orElse(null))
                .stage("Return Requested")
                .description(desc)
                .build();
        trackingRepo.save(orderTracking1);

        Orders orders1 = orderRepo.findById(orderId).orElseThrow();
        orders1.setStatus("Return Requested");
        orders1.setOrderedAt(LocalDateTime.now());
        orders1.setReturnReason(desc);
        orderRepo.save(orders1);

    }

    public String deleteCategory(int catId) {

        cateRepo.deleteById(catId);
        return "success";
    }


    public String updateProd(int prodId, ResponseOrderDetails.ProductDto productDto) {
        Products products = prodRepo.findById(prodId).orElseThrow(()-> new RuntimeException());
        if(productDto.getStock() != 0){
            products.setStock(productDto.getStock());
        }
        if(productDto.getDescription() != null){
            products.setDescription(productDto.getDescription());
        }
        if(productDto.getTitle() != null){
            products.setTitle(productDto.getTitle());
        }
        if(productDto.getPrice() != 0){
            products.setPrice(productDto.getPrice());
        }
        if(productDto.getImageURL() != null){
            products.setImageURL(productDto.getImageURL());
        }

        products.setMargin(productDto.getMargin());


        prodRepo.save(products);

        return "done";


    }


    public List<Products> getSimilarVarietyOfProduct(int productId) {

        Products product = prodRepo.findById(productId).orElse(null);
        if (product == null) return List.of();

        return prodRepo.findByBrandAndIdNot(
                product.getBrand(),
                productId,
                PageRequest.of(0, 5)
        );
    }


}
