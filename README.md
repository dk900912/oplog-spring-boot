<p align="center">
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-17+-green?logo=java&logoColor=white" alt="Java support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/dk900912/oplog-spring-boot?color=4D7A97&logo=apache" alt="License"></a>
<a href="https://search.maven.org/search?q=a:oplog-spring-boot-starter"><img src="https://img.shields.io/maven-central/v/io.github.dk900912/oplog-spring-boot-starter?logo=apache-maven" alt="Maven Central"></a>
<a href="https://github.com/dk900912/oplog-spring-boot/stargazers"><img src="https://img.shields.io/github/stars/dk900912/oplog-spring-boot" alt="GitHub Stars"></a>
<a href="https://github.com/dk900912/oplog-spring-boot/fork"><img src="https://img.shields.io/github/forks/dk900912/oplog-spring-boot" alt="GitHub Forks"></a>
<a href="https://github.com/dk900912/oplog-spring-boot/issues"><img src="https://img.shields.io/github/issues/dk900912/oplog-spring-boot" alt="GitHub issues"></a>
<a href="https://github.com/dk900912/oplog-spring-boot/graphs/contributors"><img src="https://img.shields.io/github/contributors/dk900912/oplog-spring-boot" alt="GitHub Contributors"></a>
<a href="https://github.com/dk900912/oplog-spring-boot"><img src="https://img.shields.io/github/repo-size/dk900912/oplog-spring-boot" alt="GitHub repo size"></a>
</p>

---
本组件已经发布到 maven 中央仓库，依赖于 Spring Boot 3.0+、JDK 17+，大家可以体验一下。GAV信息如下：
```xml
<dependency>
	<groupId>io.github.dk900912</groupId>
	<artifactId>oplog-spring-boot-starter</artifactId>
	<version>1.4.2</version>
</dependency>
```
## 1 快速上手

分别实现`OperatorService`和`LogRecordPersistenceService`接口，并将实现类声明为一个 Bean。更多拓展点，请大家自行阅读源码！！！

#### 1.1 声明式风格
```java
@Validated
@RestController
@RequestMapping(path = "/customer/v1/vpc")
public class VpcController {

   @OperationLog(
           bizCategory = BizCategory.FIND,
           bizTarget = "VPC", bizNo = "#target")
   @GetMapping
   public AppResult get(@RequestParam("target") String target) {
      return AppResult.builder().code(200).build();
   }

   @OperationLog(
           bizCategory = BizCategory.UPDATE,
           bizTarget = "VPC",
           bizNo = "#vpc.id",
           diffSelector = "io.github.xiaotou.oplog.VpcService#findVpcById(Long)"
   )
   @PostMapping
   public AppResult post(@RequestBody Vpc vpc) {
      return AppResult.builder().build();
   }
}
```
#### 1.2 编程式风格
```java
final SimpleOperationLogCallback<Object, Throwable> simpleOperationLogCallback
        = new SimpleOperationLogCallback<>(BizCategory.UPDATE, "VPC", 123L, bizNo -> vpcService.findVpcById((long)bizNo)) {
    @Override
    public Object doBizAction() {
        System.out.println("=== UPDATE VPC ===");
        return "success";
    }
};
operationLogTemplate.execute(simpleOperationLogCallback);
```

## 2 进阶

1. 支持多租户，其实一个租户往往就是一个特定服务，比如：订单服务。租户信息可以通过`spring.oplog.tenant`配置项来指定。

2. 为什么要为 <b>OperationLogPointcutAdvisor</b> 设定 order 属性呢？或者说为什么对外提供`spring.oplog.advisor.order`配置项呢？OperationLog 注解并不局限于 Controller 层面，也可以将其用于 Service 中的业务方法。但无论用于哪一层级，往往需要定制 Advisor 的顺序。比如：当  OperationLog 注解应用于一个
Transactional 业务方法上，那就一定要确保 `OperationLogPointcutAdvisor` 优先级高于 `BeanFactoryTransactionAttributeSourceAdvisor`，否则 OperationLogPointcutAdvisor 中的切面逻辑（持久化、RPC调用等）会拉长整个事务，如果大家想避免这种情况，那么这里就可以自行配置。

3. 在同一个类中，如果业务方法 A 调用了业务方法 B，且 A 和 B 这俩方法都由 @OperationLog 标记，那么 B 方法中并不会记录操作日志，这是 Spring AOP 的老问题了，官方也提供了解决方法，比如使用`AopContext.currentProxy()`。

4. 在不同的类中，如果类 A 中方法 m1 调用了 类 B 中方法 m2，且 m1 与 m2 均由 @OperationLog 标记，那么在解析 **bizNo** 的过程中会不会串了呢？不会。

5. 在数据更新场景中，往往需要对同一个类型的实例进行`diff`，用于实现某人对哪些字段内容进行了修改以及修改前后的内容。diff 功能依托于开源组件，而如何实现更新前后的实例查询（一般就是根据业务 ID 从数据库中查询一条数据）呢？有两个想法：
   
   1）定义一个`DiffSelector`接口，接入方可能需要定义非常多的实现类，对于接入方来说非常不友好；
   
   2）完全依托于`@DiffSelector`注解，该注解需要指定接入方 Service Bean 的名称、方法名、参数、参数类型，然后解析并反射调用方法，但这样会搞得`@OperationLog`注解很臃肿，难看。

   > 从`@RequestMapping`注解得到了灵感，定义一个`@DiffSelector`注解，接入方将该注解标记在相关 Service Bean 的实例查询方法上，那么在程序启动阶段自动探测并构建`方法名`与`DiffSelectorMethod`实例的映射关系，后续接入方只需要在`@OperationLog`注解中指定方法名即可。

6. 业务 ID 并不局限于 String，也可以是 int、long 等，而 bizNo 解析出来的一定是一个 String 类型，所以这里涉及一个类型转换，直接使用`ConversionService`实现的
```java
    private Object convertBizNoIfNecessary(Object bizNo, Class<?> bizNoClazz) {
        if (conversionService.canConvert(bizNoClazz, bizNoClazz)) {
            try {
                return conversionService.convert(bizNo, bizNoClazz);
            } catch (ConversionFailedException e) {
                logger.warn("BizNo convert failed, from {} to {}", bizNo.getClass(), bizNoClazz);
            }
        } else {
            logger.warn("ConversionService can not convert this bizNo, bizNo = {}", bizNo);
        }
        return bizNo;
    }
```

7. `diff`仅仅支持普通的数据类型（基础数据类型、LocalDate、LocalDateTime、ZonedDateTime、LocalTime、Date 等），不支持集合等类型，但这一点应该是刚好够用了。

8. `diff`结果在并发场景下是有可能串掉的，但这并不是本组件的 bug，应该是大家没有做好“对共享资源的互斥访问”吧。

9. 在运行过程中，可能会提示若干条日志，如：`Bean 'operationLogTemplate' of type [io.github.dk900912.oplog.support.OperationLogTemplate] is not eligible for getting processed by all BeanPostProcessors (for example: not eligible for auto-proxying)` 。大家不用慌张，直接忽略就好了，因为本组件声明的 Bean 并不需要走一遍所有的 BPP（比如有一个比较重要的 BPP 是用来生成代理 Bean 的，本组件所声明的 Bean 同样不需要为其生成代理类）。

10. 在编程式更新场景中，DiffSelector 如何指定呢？直接塞进去一个`Function`即可，比如：`bizNo -> vpcService.findVpcById((long)bizNo)`。