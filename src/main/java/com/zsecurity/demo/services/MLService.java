package com.zsecurity.demo.services;

import com.zsecurity.demo.entity.Products;
import com.zsecurity.demo.repositories.OrderRepo;
import com.zsecurity.demo.repositories.ProdRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MLService {

    @Autowired
    private OrderRepo orderRepository;

    @Autowired
    private ProdRepo productRepository;

    private final String mlServiceUrl;

    private final WebClient webClient;

    public MLService() {
        this.mlServiceUrl = System.getenv().getOrDefault("ML_SERVICE_URL", "http://localhost:8000");
        this.webClient = WebClient.builder()
                .baseUrl(mlServiceUrl)
                .build();
    }

    // ------------------------------
    // APRIORI
    // ------------------------------
    public List<Products> getRecommendation(String product) {

        // Step 1: Fetch order-product data
        List<Object[]> rows = orderRepository.fetchOrderProducts();

        // Step 2: Group products by orderId
        Map<Long, List<String>> grouped = new HashMap<>();

        for (Object[] row : rows) {
            Long orderId = ((Number) row[0]).longValue();
            String productName = ((String) row[1]).toLowerCase().trim();

            grouped
                    .computeIfAbsent(orderId, k -> new ArrayList<>())
                    .add(productName);
        }

        // Step 3: Convert to transactions (remove single-item orders)
        List<List<String>> transactions = grouped.values()
                .stream()
                .filter(list -> list.size() > 1)
                .collect(Collectors.toList());

        // Debug log
        System.out.println("Transactions sent to ML: " + transactions);
        System.out.println("Product clicked: " + product);

        // Step 4: Prepare request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("transactions", transactions);
        requestBody.put("product", product.toLowerCase().trim());

        // Step 5: Call FastAPI ML service
        Map<String, Object> response = webClient.post()
                .uri("/recommend")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        System.out.println("ML Response: " + response);

        // Step 6: Extract recommended product names safely
        List<String> recommendedNames =
                (List<String>) response.getOrDefault("recommended_items", new ArrayList<>());

        if (recommendedNames.isEmpty()) {
            return Collections.emptyList();
        }

        // Step 7: Fetch full product details from DB
        return recommendedNames.stream()
                .map(productRepository::findFirstByTitleContainingIgnoreCase)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    // ------------------------------
    // SIMILAR PRODUCTS
    // ------------------------------
    public String getSimilarProducts(int productId) {

        Products products = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        List<Products> allProducts = productRepository.findByCategories_Id(products.getCategories().getId());

        System.out.println(allProducts);
        List<Map<String, Object>> productList = allProducts.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("title", p.getTitle());
                    map.put("description", p.getDescription());
                    map.put("price", p.getPrice());
                    map.put("stock", p.getStock());
                    map.put("imageURL", p.getImageURL());
                    map.put("category_id", p.getCategories().getId());
                    return map;
                })
                .toList();

        System.out.println(productList);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("product_id", productId);
        requestBody.put("products", productList);

        return webClient.post()
                .uri("/similar-products")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getSmartBundles() {

        List<Object[]> rows = orderRepository.fetchOrderProducts();

        Map<Long, List<String>> grouped = new HashMap<>();

        for (Object[] row : rows) {
            Long orderId = ((Number) row[0]).longValue();
            String productName = (String) row[1];

            grouped
                    .computeIfAbsent(orderId, k -> new ArrayList<>())
                    .add(productName.toLowerCase());
        }

        List<List<String>> transactions = new ArrayList<>(grouped.values());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("transactions", transactions);

        return webClient.post()
                .uri("/bundles")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // ------------------------------
    // CART RECOMMENDATION
    // ------------------------------
    public List<Products> getCartRecommendation(List<String> cartItems) {

        // Step 1: Fetch order-product data
        List<Object[]> rows = orderRepository.fetchOrderProducts();

        Map<Long, List<String>> grouped = new HashMap<>();

        for (Object[] row : rows) {
            Long orderId = ((Number) row[0]).longValue();
            String productName = ((String) row[1]).toLowerCase().trim();

            grouped
                    .computeIfAbsent(orderId, k -> new ArrayList<>())
                    .add(productName);
        }

        // Convert to transactions
        List<List<String>> transactions = grouped.values()
                .stream()
                .filter(list -> list.size() > 1)
                .collect(Collectors.toList());

        System.out.println("Cart Items: " + cartItems);
        System.out.println("Transactions sent to ML: " + transactions);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("transactions", transactions);
        requestBody.put("cart_items",
                cartItems.stream()
                        .map(String::toLowerCase)
                        .toList());

        Map<String, Object> response = webClient.post()
                .uri("/cart-recommend")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        System.out.println("Cart ML Response: " + response);

        List<String> recommendedNames =
                (List<String>) response.getOrDefault("recommended", new ArrayList<>());

        if (recommendedNames.isEmpty()) {
            return Collections.emptyList();
        }

        return recommendedNames.stream()
                .map(productRepository::findFirstByTitleContainingIgnoreCase)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}