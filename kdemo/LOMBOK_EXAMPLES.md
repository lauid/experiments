# Java 代码简化方法对比

## 1. Lombok（推荐）

### 优点：
- 最流行的解决方案
- 注解驱动，代码简洁
- IDE 支持好
- 编译时生成，运行时无依赖

### 常用注解：
```java
@Data                    // getter, setter, toString, equals, hashCode
@Getter                  // 只生成 getter
@Setter                  // 只生成 setter
@NoArgsConstructor       // 无参构造函数
@AllArgsConstructor      // 全参构造函数
@Builder                 // 建造者模式
@Slf4j                   // 自动生成 log 变量
```

### 示例：
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String name;
    private int age;
    private String email;
}
```

## 2. Record（Java 14+）

### 优点：
- Java 原生支持
- 不可变对象
- 自动生成 getter、equals、hashCode、toString
- 编译时生成

### 示例：
```java
public record User(String name, int age, String email) {
    // 自动生成所有方法
}
```

### 限制：
- 字段是 final 的（不可变）
- 不能继承其他类
- 适合 DTO 和值对象

## 3. IDE 自动生成

### IntelliJ IDEA：
- `Alt + Insert` → 选择要生成的方法
- `Ctrl + Shift + A` → "Generate" → 选择方法

### Eclipse：
- `Alt + Shift + S` → 选择要生成的方法

### VS Code：
- 安装 Java 扩展包
- 右键 → "Source Action" → 选择方法

## 4. 代码模板

### 创建代码模板：
```java
// 在 IDE 中创建模板
public class $CLASS_NAME$ {
    private $TYPE$ $FIELD_NAME$;
    
    public $TYPE$ get$FIELD_NAME_CAPITALIZED$() {
        return $FIELD_NAME$;
    }
    
    public void set$FIELD_NAME_CAPITALIZED$($TYPE$ $FIELD_NAME$) {
        this.$FIELD_NAME$ = $FIELD_NAME$;
    }
}
```

## 5. 实际项目建议

### 对于 DTO 类：
```java
// 推荐使用 Lombok
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;
}
```

### 对于不可变对象：
```java
// 推荐使用 Record
public record Point(int x, int y) {}
```

### 对于实体类：
```java
// 使用 Lombok + JPA 注解
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String email;
}
```

## 6. 性能对比

| 方法 | 编译时 | 运行时 | 内存占用 | 推荐度 |
|------|--------|--------|----------|--------|
| Lombok | 生成字节码 | 无开销 | 低 | ⭐⭐⭐⭐⭐ |
| Record | 生成字节码 | 无开销 | 低 | ⭐⭐⭐⭐⭐ |
| IDE 生成 | 手动 | 无开销 | 低 | ⭐⭐⭐ |
| 反射 | 无 | 有开销 | 高 | ⭐⭐ |

## 7. 迁移建议

### 新项目：
1. 优先使用 Lombok
2. 不可变对象使用 Record
3. 配置 IDE 支持

### 现有项目：
1. 逐步引入 Lombok
2. 新类使用简化方法
3. 旧类逐步重构

## 8. 配置示例

### Maven 配置：
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### IDE 配置：
- 安装 Lombok 插件
- 启用注解处理
- 配置代码检查规则

## 总结

- **Lombok**：最全面的解决方案，适合大多数场景
- **Record**：适合不可变对象，Java 原生支持
- **IDE 生成**：适合简单场景，手动控制
- **组合使用**：根据具体需求选择合适的方法

推荐在新项目中使用 Lombok + Record 的组合，可以大大减少样板代码，提高开发效率。 