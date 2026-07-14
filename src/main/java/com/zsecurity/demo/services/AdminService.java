package com.zsecurity.demo.services;

import com.zsecurity.demo.dtos.*;
import com.zsecurity.demo.entity.*;
import com.zsecurity.demo.repositories.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    AuthServices authServices;

    @Autowired
    UserRepo userRepo;

    @Autowired
    ProdServices prodServices;

    @Autowired
    CateRepo cateRepo;

    @Autowired
    ProdRepo prodRepo;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    TrackingRepo trackingRepo;

    @Autowired
    EmailService emailService;

    @Autowired
    FestivalService festivalService;

    public RefundRequest.UserDTO logIn(@Valid Users users, HttpServletResponse response) {
        Users users1 = userRepo.findByEmail(users.getEmail()).orElseThrow();

        if (users1.getRoles().name().equals("ADMIN")) {
            return authServices.logIn(users, response);
        } else {
            throw new RuntimeException("Invalid Admin Credentials");
        }
    }

    public List<Products> getAllProducts() {
        return prodServices.getAllProducts();
    }

    public Products addProduct(ResponseOrderDetails.ProductDto products) {
        Categories categories = cateRepo.findById(products.getCatId()).orElseThrow();
        Products products1 = Products.builder()
                .title(products.getTitle())
                .price(products.getPrice())
                .imageURL(products.getImageURL())
                .title(products.getTitle())
                .description(products.getDescription())
                .categories(categories)
                .stock(products.getStock())
                .margin(products.getMargin())
                .build();
        return prodServices.addProduct(products1);
    }

    public Categories addCategories(Categories categories) {
        return prodServices.addCategories(categories);
    }

    public Products assignCategoryToProduct(int pid, int cid) {
        return prodServices.assignCategoryToProduct(pid, cid);
    }

    public void deleteUserById(@PathVariable long id) {
        userRepo.deleteById(id);
    }

    public List<RefundRequest.UserDTO> getAllUserReg() {
        List<Users> users = userRepo.findAll();
        return users.stream().map(users1 -> RefundRequest.UserDTO.builder()
                .id(users1.getId())
                .dob(users1.getDob())
                .roles(String.valueOf(users1.getRoles()))
                .firstName(users1.getFirstName())
                .email(users1.getEmail())
                .lastName(users1.getLastName())
                .gender(users1.getGender())
                .imageURL(users1.getImageLink())
                .isActiveNow(users1.getIsActiveNow())
                .isActive(users1.getIsActive())
                .build()
        ).toList();
    }

    public void disableUser(String email) {
        Users users = userRepo.findByEmail(email).orElseThrow();
        users.setIsActiveNow(false);
        users.setIsActive(false);
        userRepo.save(users);
    }

    public void updateTrackRecord(int orderId, String stage, String desc) {
        OrderTracking orderTracking1 = OrderTracking.builder()
                .updatedBy("ADMIN")
                .updatedTime(LocalDateTime.now())
                .ordersId(orderRepo.findById(orderId).orElse(null))
                .stage(stage)
                .description(desc)
                .build();
        trackingRepo.save(orderTracking1);

        Orders orders1 = orderRepo.findById(orderId).orElseThrow();
        orders1.setStatus(stage);
        orders1.setOrderedAt(LocalDateTime.now());
        orders1.setDescription(desc);
        orderRepo.save(orders1);

        if (stage.equals("Returned")) {
            returnOfProd(orders1);
        }
        emailService.emailForOrderStatusUpdate(orders1.getUserId(), orders1, stage, desc);
    }

    public void returnOfProd(Orders orders) {
        List<OrderTracking> orderTrackings = trackingRepo.findByOrdersId(orders);
        List<OrderItems> orderItems = orderTrackings.getFirst().getOrdersId().getItems();
        for (OrderItems orderItem : orderItems) {
            Products products = orderItem.getProdId();
            products.setStock(orderItem.getProdId().getStock() + orderItem.getQuantity());
            prodRepo.save(products);
        }
    }

    public void deleteCategoryById(int catId) {
        cateRepo.deleteById(catId);
    }

    public void deleteProdById(int prodId) {
        prodRepo.deleteById(prodId);
    }

    public Products patchProduct(int id, ResponseOrderDetails.ProductDto req) {
        Products p = prodRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (req.getTitle() != null)
            p.setTitle(req.getTitle());

        if (req.getDescription() != null)
            p.setDescription(req.getDescription());

        if (req.getPrice() != 0)
            p.setPrice(req.getPrice());

        if (req.getStock() != 0)
            p.setStock(req.getStock());

        if (req.getImageURL() != null)
            p.setImageURL(req.getImageURL());

        if (req.getCatId() != 0) {
            Categories c = cateRepo.findById(req.getCatId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            p.setCategories(c);
        }

        return prodRepo.save(p);
    }

    public OrderAdminFull getOrderById(int orderId) {
        Orders order = orderRepo.findOrderFullDetails(orderId);

        Users user = order.getUserId();

        RefundRequest.UserDTO userDTO = RefundRequest.UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();

        List<ProductQuantityResponse> products = order.getItems()
                .stream()
                .map(item -> {
                    Products p = item.getProdId();
                    return ProductQuantityResponse.builder()
                            .productId(p.getId())
                            .productName(p.getTitle())
                            .productImage(p.getImageURL())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .build();
                })
                .toList();

        return OrderAdminFull.builder()
                .orderId(order.getId())
                .user(userDTO)
                .address(order.getUserAddressDetails())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .paymentMode(order.getPaymentMethod())
                .refundStatus(order.getRefundStatus())
                .orderedAt(order.getOrderedAt())
                .products(products)
                .discountAmount(order.getDiscountAmount())
                .reasonForReturn(order.getReturnReason())
                .build();
    }

    public List<Categories> getAllCategories() {
        return cateRepo.findAll();
    }

    public List<OrderAdminLite> getAllOrdersLite() {
        List<Orders> orders = orderRepo.findAllLite();

        return orders.stream()
                .map(order -> OrderAdminLite.builder()
                        .orderId(order.getId())
                        .fullName(order.getUserAddressDetails().getFullName())
                        .email(order.getUserId().getEmail())
                        .totalPrice(order.getTotalPrice())
                        .status(order.getStatus())
                        .paymentMode(order.getPaymentMethod())
                        .orderedAt(order.getOrderedAt())
                        .build()
                )
                .toList();
    }

    public void addNormalOfferToProduct(int prodId, double disPer) {
        Products products = prodRepo.findById(prodId).get();
        if (!products.isFestivalOffer() && !products.isDealOfWeek() && products.getDiscountFinalPrice() == 0) {
            products.setNormalOffer(true);
            products.setDiscountPercent(disPer);
            int finalPrice = (int) (products.getPrice() - products.getPrice() * disPer / 100);
            products.setDiscountFinalPrice(finalPrice);
            prodRepo.save(products);
        }
    }

    public void deleteProdctById(int prodId) {
        prodRepo.deleteById(prodId);
    }

    public void removeDeal(Integer productId) {
        Products product = prodRepo.findById(productId).orElseThrow();
        product.setDiscountFinalPrice(0);
        product.setDiscountPercent(0.0);
        product.setDealOfWeek(false);
        prodRepo.save(product);
    }

    public FestivalProduct addFestivalProduct(FestivalProduct request) {
        return festivalService.addFestivalProduct(request);
    }

    public List<ResponseOrderDetails.ShowProd> mapToShowProd(List<Products> products) {
        return products.stream()
                .map(p -> ResponseOrderDetails.ShowProd.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .description(p.getDescription())
                        .price(p.getPrice())
                        .stock(p.getStock())
                        .category(p.getCategories().getName())
                        .margin(p.getMargin())
                        .imageURL(p.getImageURL())
                        .build())
                .toList();
    }

    public void setDealOfWeek(int productId, double discount) {
        Products product = prodRepo.findById(productId).orElseThrow();
        product.setDiscountFinalPrice((int) (product.getPrice() - (product.getPrice() * discount / 100)));
        product.setDealOfWeek(true);
        product.setDiscountPercent(discount);
        prodRepo.save(product);
    }
}