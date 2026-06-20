# Spring Boot Microservices Revision Notes

These notes explain the flow we practiced step by step:

```text
Simple controller
  -> Two microservices
  -> Communication using RestTemplate / RestClient
  -> Communication using Feign
  -> Eureka Naming Server
  -> Multiple instances + Load Balancing
  -> API Gateway
```

The main example:

```text
currency-conversion-service
        calls
currency-exchange-service
```

---

## 1. Start With A Simple Controller

First we create one simple microservice, for example:

```text
currency-exchange-service
```

Its job:

```text
Given from currency and to currency, return exchange rate.
```

Example endpoint:

```text
GET http://localhost:8000/currency-exchange/from/USD/to/INR
```

Example response:

```json
{
  "id": 1000,
  "from": "USD",
  "to": "INR",
  "conversionMultiple": 80,
  "environment": "8000"
}
```

Controller idea:

```java
@RestController
public class CurrencyExchangeController {

    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    public CurrencyExchange retrieveExchangeValue(
            @PathVariable String from,
            @PathVariable String to) {
        return new CurrencyExchange(1000L, from, to, BigDecimal.valueOf(80));
    }
}
```

We also need a response class:

```java
public class CurrencyExchange {
    private Long id;
    private String from;
    private String to;
    private BigDecimal conversionMultiple;
    private String environment;
}
```

At this point, this service is independent. It does not call any other microservice.

---

## 2. Create Second Microservice

Now create:

```text
currency-conversion-service
```

Its job:

```text
Take quantity and exchange rate, then calculate total amount.
```

Example endpoint:

```text
GET http://localhost:8100/currency-conversion/from/USD/to/INR/quantity/10
```

Expected logic:

```text
quantity * conversionMultiple
10 * 80 = 800
```

But conversion service does not know the exchange rate itself. It must call:

```text
currency-exchange-service
```

So now we need microservice-to-microservice communication.

---

## 3. First Communication: Direct REST Call

First simple way:

```text
currency-conversion-service directly calls currency-exchange-service using URL.
```

Flow:

```text
Browser
  -> currency-conversion-service :8100
      -> calls http://localhost:8000/currency-exchange/from/USD/to/INR
          -> currency-exchange-service :8000
```

Controller idea:

```java
@GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
public CurrencyConversion calculateCurrencyConversion(
        @PathVariable String from,
        @PathVariable String to,
        @PathVariable BigDecimal quantity) {

    ResponseEntity<CurrencyConversion> responseEntity =
            new RestTemplate().getForEntity(
                    "http://localhost:8000/currency-exchange/from/{from}/to/{to}",
                    CurrencyConversion.class,
                    from,
                    to);

    CurrencyConversion response = responseEntity.getBody();

    return new CurrencyConversion(
            response.getId(),
            from,
            to,
            quantity,
            response.getConversionMultiple(),
            quantity.multiply(response.getConversionMultiple()),
            response.getEnvironment());
}
```

For this approach, we also need a response class in conversion service, for example:

```java
public class CurrencyConversion {
    private Long id;
    private String from;
    private String to;
    private BigDecimal quantity;
    private BigDecimal conversionMultiple;
    private BigDecimal totalCalculatedAmount;
    private String environment;
}
```

Problem with this approach:

```text
http://localhost:8000 is hardcoded.
```

If exchange service runs on another port, we must change the code.

---

## 4. Better Communication: Feign

RestTemplate / RestClient works, but code becomes lengthy.

Feign gives a better approach:

```text
Create a Java interface.
Feign converts method calls into HTTP calls.
```

First enable Feign in conversion service:

```java
@SpringBootApplication
@EnableFeignClients
public class CurrencyConversionServiceApplication {
}
```

Create proxy interface:

```java
@FeignClient(name = "currency-exchange-service", url = "localhost:8000")
public interface CurrencyExchangeProxy {

    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    CurrencyConversion retrieveExchangeValue(
            @PathVariable String from,
            @PathVariable String to);
}
```

Then use it in controller:

```java
private final CurrencyExchangeProxy proxy;

public CurrencyConversionController(CurrencyExchangeProxy proxy) {
    this.proxy = proxy;
}

@GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
public CurrencyConversion calculateCurrencyConversionFeign(
        @PathVariable String from,
        @PathVariable String to,
        @PathVariable BigDecimal quantity) {

    CurrencyConversion response = proxy.retrieveExchangeValue(from, to);

    return new CurrencyConversion(
            response.getId(),
            from,
            to,
            quantity,
            response.getConversionMultiple(),
            quantity.multiply(response.getConversionMultiple()),
            response.getEnvironment());
}
```

Now communication is cleaner.

But there is still one problem:

```java
url = "localhost:8000"
```

The URL is still hardcoded.

If we run another instance of exchange service on port `8001`, Feign will still call only `8000`.

So we need service discovery.

---

## 5. Add Eureka Naming Server

To remove hardcoded URLs, we add:

```text
naming-server
```

Eureka works like a phonebook:

```text
Application name -> list of running instance URLs
```

Example:

```text
CURRENCY-EXCHANGE-SERVICE
  -> localhost:8000
  -> localhost:8001

CURRENCY-CONVERSION-SERVICE
  -> localhost:8100
```

Create a new Spring Boot app:

```text
naming-server
```

Add Eureka Server dependency:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

Enable Eureka Server:

```java
@SpringBootApplication
@EnableEurekaServer
public class NamingServerApplication {
}
```

Configure `naming-server/src/main/resources/application.properties`:

```properties
spring.application.name=naming-server
server.port=8761

eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

Run it and open:

```text
http://localhost:8761
```

At first, no client services are listed.

---

## 6. Register Microservices As Eureka Clients

Now each microservice must register itself with Eureka.

Add Eureka Client dependency in:

```text
currency-exchange-service
currency-conversion-service
```

Dependency:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

Configure exchange service:

```properties
spring.application.name=currency-exchange-service
server.port=8000
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

Configure conversion service:

```properties
spring.application.name=currency-conversion-service
server.port=8100
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

Run in this order:

```text
1. naming-server
2. currency-exchange-service
3. currency-conversion-service
```

Now Eureka dashboard should show both services.

---

## 7. Feign With Eureka

Now Feign does not need hardcoded URL.

Before:

```java
@FeignClient(name = "currency-exchange-service", url = "localhost:8000")
```

After:

```java
@FeignClient(name = "currency-exchange-service")
```

Now this name:

```text
currency-exchange-service
```

is resolved using Eureka.

Flow:

```text
currency-conversion-service
  -> Feign proxy
      -> asks Eureka: where is currency-exchange-service?
          -> Eureka returns an instance URL
              -> Feign calls that instance
```

Now we do not care if exchange service is on `8000`, `8001`, or another port.

---

## 8. Run Multiple Instances

Now run exchange service on multiple ports:

```text
currency-exchange-service :8000
currency-exchange-service :8001
```

Example command:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8001
```

Eureka will show:

```text
CURRENCY-EXCHANGE-SERVICE
  -> localhost:8000
  -> localhost:8001
```

Now when conversion service calls exchange service using Feign:

```java
@FeignClient(name = "currency-exchange-service")
```

Spring Cloud LoadBalancer chooses one instance.

Example:

```text
first request  -> exchange service on 8000
second request -> exchange service on 8001
third request  -> exchange service on 8000
```

The `environment` field helps us verify which instance responded.

Important point:

```text
Eureka stores the instances.
Spring Cloud LoadBalancer chooses which instance to call.
```

Eureka itself is the registry, not the actual load balancer.

---

## 9. Add API Gateway

Now communication works, but there are many URLs:

```text
currency-exchange-service    -> http://localhost:8000
currency-conversion-service  -> http://localhost:8100
eureka                       -> http://localhost:8761
```

For clients, we want one common entry point:

```text
api-gateway
```

Usually:

```text
http://localhost:8765
```

Add API Gateway dependency:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
</dependency>
```

Also add Eureka Client dependency:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

Configure `api-gateway/src/main/resources/application.properties`:

```properties
spring.application.name=api-gateway
server.port=8765

eureka.client.service-url.defaultZone=http://localhost:8761/eureka

spring.cloud.gateway.server.webflux.discovery.locator.enabled=true
spring.cloud.gateway.server.webflux.discovery.locator.lower-case-service-id=true
```

Run:

```text
1. naming-server
2. currency-exchange-service
3. currency-conversion-service
4. api-gateway
```

Now we can call services through gateway.

Example:

```text
http://localhost:8765/currency-exchange-service/currency-exchange/from/USD/to/INR
```

Gateway sees:

```text
currency-exchange-service
```

Then it asks Eureka:

```text
Where is currency-exchange-service?
```

Then it routes to:

```text
localhost:8000 or localhost:8001
```

Similarly:

```text
http://localhost:8765/currency-conversion-service/currency-conversion-feign/from/USD/to/INR/quantity/10
```

routes to:

```text
currency-conversion-service
```

---

## Final Flow

```text
Client
  -> API Gateway :8765
      -> Eureka lookup
          -> currency-conversion-service :8100
              -> Feign
                  -> Eureka lookup
                      -> LoadBalancer
                          -> currency-exchange-service :8000 / :8001
```

---

## Full Learning Order

```text
1. Create currency-exchange-service with a simple controller.
2. Create CurrencyExchange response class.
3. Create currency-conversion-service.
4. Make conversion service call exchange service using direct REST URL.
5. Notice direct REST code is lengthy and URL is hardcoded.
6. Add Feign to make communication cleaner.
7. Notice Feign still has hardcoded URL.
8. Add Eureka Naming Server.
9. Register both microservices as Eureka clients.
10. Change Feign to use only application name, not URL.
11. Run multiple instances of exchange service.
12. Let Spring Cloud LoadBalancer pick an instance.
13. Add API Gateway.
14. Use one gateway URL to access different microservices.
```

---

## Short Memory Trick

```text
Direct URL
  -> Feign
  -> Eureka
  -> LoadBalancer
  -> API Gateway
```

Meaning:

```text
First make it work.
Then make the communication cleaner.
Then remove hardcoded URLs.
Then support multiple instances.
Then expose everything through one gateway.
```