# Food Delivery Microservices with Eureka (Dynamic Service Registration)

Spring Boot 3.3.4, Spring Cloud 2023.0.3, Java 17. Eight independent Maven projects.

```
                         Client / Postman
                                |
                        api-gateway :8080
                                |   lb://<service-name>   (looks up Eureka)
        +-----------+-----------+-----------+-----------+-----------+
   restaurant    menu       customer      order       payment     delivery
    service    service      service      service      service     service
        \_________\__________\_____ all register with ______/_________/
                         eureka-server :8761
```

| Project            | Port | Registered name      | Job |
|--------------------|------|----------------------|-----|
| eureka-server      | 8761 | -                    | Service registry + dashboard |
| api-gateway        | 8080 | API-GATEWAY          | Single entry point, routes by service name |
| restaurant-service | random | RESTAURANT-SERVICE | Restaurants (CRUD) |
| menu-service       | random | MENU-SERVICE       | Menu items of each restaurant (CRUD) |
| customer-service   | random | CUSTOMER-SERVICE   | Customers and addresses (CRUD) |
| order-service      | random | ORDER-SERVICE      | Places orders: calls customer, restaurant, menu, payment and delivery services through Eureka |
| payment-service    | random | PAYMENT-SERVICE    | Simulated payment (SUCCESS / FAILED) |
| delivery-service   | random | DELIVERY-SERVICE   | Assigns a driver, tracks delivery status |

Every service uses layers: `model` -> `service` -> `controller`. Data is kept in memory (no database).

## Import into STS
File > Import > Maven > Existing Maven Projects > Root Directory = the extracted `eureka-demo` folder > select all 8 projects > Finish. Wait for Maven to download dependencies.

## Run order
1. `EurekaServerApplication` (wait until it is up)
2. restaurant, menu, customer, payment, delivery services (any order)
3. `OrderServiceApplication`
4. `ApiGatewayApplication`

Open http://localhost:8761 - all services should show **UP**.
Wait about 30 seconds after starting everything so each service has fetched the registry, then test.

## Test through the gateway (port 8080 never changes)
```
GET  http://localhost:8080/restaurants
GET  http://localhost:8080/menu/restaurant/3
GET  http://localhost:8080/customers

POST http://localhost:8080/orders
Content-Type: application/json
{
  "customerId": 1,
  "restaurantId": 3,
  "paymentMethod": "UPI",
  "items": [ {"menuItemId": 5, "quantity": 2}, {"menuItemId": 6, "quantity": 1} ]
}

GET  http://localhost:8080/orders
GET  http://localhost:8080/orders/customer/1
GET  http://localhost:8080/deliveries
PUT  http://localhost:8080/deliveries/1/status?status=DELIVERED
GET  http://localhost:8080/payments
```
Menu items 1-2 belong to restaurant 1, 3-4 to restaurant 2, 5-6 to restaurant 3. An item from another restaurant is rejected with 400.
You can also use the automatic route: `http://localhost:8080/restaurant-service/restaurants`.

## What happens when an order is placed
1. order-service calls `http://customer-service/customers/{id}` and `http://restaurant-service/restaurants/{id}`
2. calls `http://menu-service/menu/{id}` for every item to get the price and calculates the total
3. calls `http://payment-service/payments` (POST)
4. if payment is SUCCESS, calls `http://delivery-service/deliveries` (POST) and a driver is assigned
5. order status becomes CONFIRMED

The URLs use service names, not ports. `@LoadBalanced RestTemplate` asks Eureka for a running instance, which is why random ports do not break anything.
Error cases: unknown customer/restaurant/item -> 404, a service that is down -> 503, payment failed -> status PAYMENT_FAILED.

## Other endpoints
| Service    | Endpoints |
|------------|-----------|
| restaurant | GET/POST `/restaurants`, GET/PUT/DELETE `/restaurants/{id}` |
| menu       | GET/POST `/menu`, GET/PUT/DELETE `/menu/{id}`, GET `/menu/restaurant/{restaurantId}` |
| customer   | GET/POST `/customers`, GET/PUT/DELETE `/customers/{id}` |
| order      | POST/GET `/orders`, GET `/orders/{id}`, GET `/orders/customer/{customerId}`, PUT `/orders/{id}/cancel` |
| payment    | POST/GET `/payments`, GET `/payments/{id}`, GET `/payments/order/{orderId}` |
| delivery   | POST/GET `/deliveries`, GET `/deliveries/{id}`, GET `/deliveries/order/{orderId}`, PUT `/deliveries/{id}/status?status=` |

Every service also has `/actuator/health` and `/actuator/info`.
