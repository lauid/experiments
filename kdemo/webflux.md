# Spring Boot WebFlux 与 Web 共存时的全局拦截器与异常处理器配置总结

在 Spring Boot 项目中，如果同时引入了 `spring-boot-starter-web`（Spring MVC）和 `spring-boot-starter-webflux`（WebFlux），可以实现"局部响应式"，即部分接口用 WebFlux，其他接口用传统 MVC。此时，全局拦截器和异常处理器的配置需要注意以下几点：

---

## 1. 全局异常处理器

- **Spring MVC（Web）异常处理器**
  - 使用 `@ControllerAdvice` + `@ExceptionHandler`，处理同步 Controller 的异常。
  - 只会拦截 Spring MVC（即返回普通对象的 Controller）抛出的异常。

- **WebFlux 异常处理器**
  - 也可以用 `@ControllerAdvice` + `@ExceptionHandler`，但需返回 `Mono`/`Flux` 类型，或实现 `org.springframework.web.server.WebExceptionHandler` 接口。
  - 只会拦截 WebFlux（即返回 Mono/Flux 的 Controller）抛出的异常。

**注意：**
- Spring MVC 的异常处理器不会处理 WebFlux 的异常，反之亦然。
- 如果希望两种 Controller 都能被统一处理，需要分别为 MVC 和 WebFlux 各写一套异常处理器。

**示例：**

Spring MVC 异常处理器：
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(500).body("MVC异常: " + ex.getMessage());
    }
}
```

WebFlux 异常处理器：
```java
@RestControllerAdvice
public class GlobalWebFluxExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleException(Exception ex) {
        return Mono.just(ResponseEntity.status(500).body("WebFlux异常: " + ex.getMessage()));
    }
}
```
或实现 `WebExceptionHandler`：
```java
@Component
public class CustomWebExceptionHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 处理WebFlux异常
        // ...
    }
}
```

---

## 2. 全局拦截器

- **Spring MVC 拦截器**
  - 实现 `HandlerInterceptor` 接口，并通过 `WebMvcConfigurer` 注册。
  - 只拦截 Spring MVC 的请求。

- **WebFlux 拦截器**
  - 实现 `WebFilter` 或 `HandlerFilterFunction`，并通过 `WebFluxConfigurer` 注册。
  - 只拦截 WebFlux 的请求。

**注意：**
- Spring MVC 的拦截器不会拦截 WebFlux 的请求，反之亦然。
- 如果有登录校验、权限校验等需求，需要分别为两种模式配置拦截器。

**示例：**

Spring MVC 拦截器：
```java
public class MyMvcInterceptor implements HandlerInterceptor { ... }
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MyMvcInterceptor());
    }
}
```

WebFlux 拦截器：
```java
@Component
public class MyWebFluxFilter implements WebFilter { ... }
```

---

## 3. 一个Controller中既有同步方法又有WebFlux方法

在Spring Boot项目中，如果一个Controller类中既有同步方法（返回普通对象），又有WebFlux方法（返回Mono/Flux），Spring会根据方法的返回类型自动分流处理：

### 处理机制
- 同一个Controller类中可以混用同步和响应式方法，Spring Boot会自动识别每个方法的返回类型。
- 返回普通对象（如String、List、ResponseEntity等）的方法，由Spring MVC（Servlet DispatcherServlet）处理。
- 返回Mono/Flux的方法，由WebFlux（DispatcherHandler）处理。
- Spring会自动分流，不会冲突。

### 示例
```java
@RestController
@RequestMapping("/api/demo")
public class DemoController {
    // 同步方法（Spring MVC 处理）
    @GetMapping("/sync")
    public String syncHello() {
        return "Hello, Sync!";
    }

    // 响应式方法（WebFlux 处理）
    @GetMapping("/async")
    public Mono<String> asyncHello() {
        return Mono.just("Hello, WebFlux!");
    }
}
```
- 访问 `/api/demo/sync` 时，走Spring MVC同步处理链。
- 访问 `/api/demo/async` 时，走WebFlux响应式处理链。

### 注意事项
1. **异常处理和拦截器**
   - 同步方法抛出的异常，只会被MVC的异常处理器拦截。
   - 响应式方法抛出的异常，只会被WebFlux的异常处理器拦截。
   - 拦截器同理，分别走各自的链路。
2. **依赖注入**
   - 可以在同一个Controller中注入RestTemplate和WebClient，但建议不要在同一个方法中混用。
3. **全局配置**
   - 全局配置（如CORS、拦截器、异常处理）要分别为MVC和WebFlux配置。
4. **建议**
   - 虽然可以混用，但实际开发中建议一个Controller尽量风格统一，便于维护和理解。
   - 如果确实需要混用，Spring官方是支持的，不会有技术障碍。

### 总结
- 可以：一个Controller类中既有同步方法，也有WebFlux方法，Spring会自动分流处理。
- 不会冲突，但全局拦截器、异常处理器等要分开配置。
- 建议风格统一，但混用是被官方支持的。

---

## 4. 总结建议

- **需要分开配置**：全局拦截器和异常处理器，MVC 和 WebFlux 各自独立，互不影响。
- **建议**：如果项目两种模式都用，建议分别实现和注册对应的拦截器和异常处理器。
- **Bean 注入**：WebClient 适用于 WebFlux，RestTemplate 适用于 MVC，不要混用。
- **全局配置**：部分全局配置（如拦截器、异常处理）需要分别为 WebFlux 和 MVC 配置。

如需更详细的代码示例或针对具体场景的配置建议，请结合实际需求进一步完善。 