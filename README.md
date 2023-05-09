<p align="center">
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-8+-green?logo=java&logoColor=white" alt="Java support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/dk900912/oplog-spring-boot?color=4D7A97&logo=apache" alt="License"></a>
<a href="https://search.maven.org/search?q=a:oplog-spring-boot-starter"><img src="https://img.shields.io/maven-central/v/io.github.dk900912/oplog-spring-boot-starter?logo=apache-maven" alt="Maven Central"></a>
<a href="https://github.com/dk900912/oplog-spring-boot/stargazers"><img src="https://img.shields.io/github/stars/dk900912/oplog-spring-boot" alt="GitHub Stars"></a>
<a href="https://github.com/dk900912/oplog-spring-boot/fork"><img src="https://img.shields.io/github/forks/dk900912/oplog-spring-boot" alt="GitHub Forks"></a>
<a href="https://github.com/dk900912/oplog-spring-boot/issues"><img src="https://img.shields.io/github/issues/dk900912/oplog-spring-boot" alt="GitHub issues"></a>
<a href="https://github.com/dk900912/oplog-spring-boot/graphs/contributors"><img src="https://img.shields.io/github/contributors/dk900912/oplog-spring-boot" alt="GitHub Contributors"></a>
<a href="https://github.com/dk900912/oplog-spring-boot"><img src="https://img.shields.io/github/repo-size/dk900912/oplog-spring-boot" alt="GitHub repo size"></a>
</p>

## 如何使用
本组件已经发布到 maven 中央仓库，大家可以体验一下。目前建议大家使用 1.4 和 1.4.1 版本，其中 1.4.1 版本需要 Spring Boot 3.0+、JDK 17+ 哈。GAV信息如下：
```xml
<dependency>
	<groupId>io.github.dk900912</groupId>
	<artifactId>oplog-spring-boot-starter</artifactId>
	<version>1.4.1</version>
</dependency>
```
#### 声明式风格
```java
@RestController
@RequestMapping(path = "/demo/v1")
public class DemoController {
    private OperationLogDao operationLogDao;

    public DemoController(OperationLogDao operationLogDao) {
        this.operationLogDao = operationLogDao;
    }

    @OperationLog(bizCategory = BizCategory.CREATE, bizTarget = "订单", bizNo = "#appResult.data.orderId")
    @PostMapping(path = "/create")
    public AppResult create(@RequestBody OrderReq orderReq) {
        // 创建资源时，一般前端不会将ID传过来，所以一般都是从响应结果中获取bizNo哈
        orderReq.setOrderId("001");
        return AppResult.success().data(orderReq);
    }

    @OperationLog(bizCategory = BizCategory.UPDATE, bizTarget = "订单", bizNo = "#orderReq.orderId", 
            diffSelector = @DiffSelector(bean = "orderService", method = "findOrderById", param = "#orderReq.orderId"))
    @PutMapping(path = "/update")
    public AppResult update(@RequestBody OrderReq orderReq) {
        return AppResult.success();
    }

    @OperationLog(bizCategory = BizCategory.DELETE, bizTarget = "订单", bizNo = "#id")
    @DeleteMapping(path = "/delete/{id}")
    public AppResult delete(@PathVariable("id") int id) {
        return AppResult.success();
    }
}
```
#### 编程式风格
```java
operationLogRemplate.execute(
        new SimpleOperationLogCallback<String, Throwable>(BizCategory.PLACE_ORDER, "订单", "order-123456") {
            @Override
            public String doBizAction() {
                // business action
            }
        });
```

## 进阶

1. 支持多租户，其实一个租户往往就是一个特定服务，比如：订单服务。租户信息可以通过`spring.oplog.tenant`配置项来指定。


2. 为什么要为 <b>OperationLogPointcutAdvisor</b> 设定 order 属性呢？或者说为什么对外提供`spring.oplog.advisor.order`配置项呢？OperationLog 注解并不局限于 Controller 层面，也可以将其用于 Service 中的业务方法。但无论用于哪一层级，往往需要定制 Advisor 的顺序。比如：当  OperationLog 注解应用于一个
Transactional 业务方法上，那就一定要确保 `OperationLogPointcutAdvisor` 优先级高于 `BeanFactoryTransactionAttributeSourceAdvisor`，否则 OperationLogPointcutAdvisor 中的切面逻辑（持久化、RPC调用等）会拉长整个事务，这是要避免的。


3. 在同一个类中，如果业务方法 A 调用了业务方法 B，且 A 和 B 这俩方法都由 @OperationLog 标记，那么 B 方法中并不会记录操作日志，这是 Spring AOP 的老问题了，官方也提供了解决方法，比如使用`AopContext.currentProxy()`。


4. 在不同的类中，如果类 A 中方法 m1 调用了 类 B 中方法 m2，且 m1 与 m2 均由 @OperationLog 标记，那么在解析 **bizNo** 的过程中会不会串了呢？不会。


## 后续开发计划

目前，还差一个对象间的 diff 功能，目前只能由大家自行 diff 了，下半年会补上这一功能。

## 如何屏蔽
一旦引入起步依赖组件，那么将自动开启日志记录功能。除了移除依赖包之外，还可以在`application.properties`中追加以下配置来实现屏蔽目的：
```
spring.oplog.enabled=false
```
## 样例

![sample](doc/sample.png)