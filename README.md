[![Anurag's GitHub stats](https://github-readme-stats.vercel.app/api?username=dk900912)](https://github.com/anuraghazra/github-readme-stats)
 
> 若在使用过程中有任何疑问，请搜索微信公众号：*程序猿杜小头*，与我联系！！！

《如何优雅地记录操作日志》是美团技术团队2021年最受欢迎的一篇技术文章，文章很有深度，强烈建议大家去品读一番。操作日志指的是某一**时间**下**谁**对**什么**做了**什么事情**，操作日志一般限定于创建、更新和删除操作，而查询并不是什么敏感操作，所以无需记录操作日志。比如：管理员于2020-10-10 11:12:13新增一个用户，用户名为crimson_typhoon；
买家青鸟于2020-10-10 11:12:13更新了联系邮箱，更新前：111111@qq.com，更新后：222222@qq.com等。

虽然原文已经写的很深入了，但可能受限于篇幅，对**方法注解**所涉及到的Spring AOP相关知识并没有详细阐述，所以本文将着重叙述这一块内容。

老实说，基于方法注解来实现操作日志是极其优雅的方案，它可以有效收敛横切关注逻辑，避免日志操作的记录逻辑散落在各个业务类中，极大提高了代码的可读性和可维护性。有些老司机可能会觉得直接使用`Aspectj`就好了，就像下面贴出来的代码那样。**的确可以这么做，但后期不易复用，而且还有兼容性的问题**。
```java
@Component
@Aspect
public class OperationLogAdvice {
    @Around(value = "@annotation(io.github.dk900912.oplog.annotation.OperationLog)")
    public Object doOperationLog(ProceedingJoinPoint joinPoint) {
        // STEP 1：执行目标方法
        Object result = null;
        Throwable exceptionOnTraget = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            exceptionOnTraget = e;
        }
        // STEP 2：记录操作日志
        // STEP 3：如果目标方法执行失败，那么需要重新抛出异常
        return result;
    }
}
```
## 如何使用
笔者参照原文中的思路，实现了一个记录操作日志的组件；该组件已经发布到maven中央仓库，大家可以体验一下哈。GAV信息如下：
```xml
<dependency>
	<groupId>io.github.dk900912</groupId>
	<artifactId>oplog-spring-boot-starter</artifactId>
	<version>1.0.15</version>
</dependency>
```
示例代码如下：
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

    @OperationLog(bizCategory = BizCategory.UPDATE, bizTarget = "订单", bizNo = "#orderReq.orderId")
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
> 如果业务方法A调用了业务方法B，且A和B这俩方法都由@OperationLog标记，那么B方法中并不会记录操作日志，这是Spring AOP的老问题了，官方也提供了解决方法，比如使用`AopContext.currentProxy()`。
## 如何屏蔽
一旦引入起步依赖组件，那么将自动开启日志记录功能。除了移除依赖包之外，还可以在`application.properties`中追加以下配置来实现屏蔽目的：
```
oplog.enabled=false
```
## 代理对象如何生成
基于方法注解实现操作日志这一方案需要依赖于`Spring AOP`，核心思想就是借助Spring AOP去自动探测由`OperationLog`注解接口标记的业务逻辑类，从而为这些业务类动态创建代理对象。既然Spring AOP是基于代理对象来拓展目标对象的，那就很容易想到：Spring IoC容器内贮存的一定是代理对象而非目标对象，那究竟是如何替换的呢？众所周知，Spring暴露了若干IoC容器拓展点(IoC Container Extensiion Points)，`BeanPostProcessor`接口就是其中之一；有了BeanPostProcessor，任何人都可以在Bean初始化前后对其进行个性化改造，甚至将其替换。

让我们来看一下BeanPostProcessor接口中的内容，它只有两个方法，如下：
```java
public interface BeanPostProcessor {
    default Object postProcessBeforeInitialization(Object bean, String beanName) 
            throws BeansException {
        return bean;
    }
    default Object postProcessAfterInitialization(Object bean, String beanName) 
            throws BeansException {
        return bean;
    }
}
```

没错，Spring AOP就是通过BeanPostProcessor将目标对象替换为代理对象的！在Spring AOP中，这个BeanPostProcessor就是`AbstractAutoProxyCreator`抽象类，其主要用于创建代理对象。 Spring AOP为AbstractAutoProxyCreator定义了两个直系子类，分别是：`BeanNameAutoProxyCreator`和`AbstractAdvisorAutoProxyCreator`；前者根据Bean的名称来判断是否需要为当前Bean创建代理对象，后者根据**Advisor**探测结果来判断是否需要为该Bean创建代理对象；何为Advisor？Advisor是Spring AOP中独有的术语，在AspectJ中并没有等效的术语与其匹配，但其与切面还是有一定相似之处的，或者大家干脆将其视为一个特殊的切面，该切面只能包含一个Advice (通知) 和一个Pointcut (切入点) 而已；此外，Advisor有两个分支，分别是`PointcutAdvisor`和`IntroductionAdvisor`。

相较于BeanNameAutoProxyCreator，`AbstractAdvisorAutoProxyCreator`更为重要，AbstractAdvisorAutoProxyCreator有三个子类，分别是`AspectJAwareAdvisorAutoProxyCreator`、`AnnotationAwareAspectJAutoProxyCreator`和`InfrastructureAdvisorAutoProxyCreator`。一般，在Spring IoC中只会有一个名称为org.springframework.aop.config.internalAutoProxyCreator、类型为AbstractAdvisorAutoProxyCreator的Bean，如果classpath下没有Spring AOP依赖或者没有aspectjweaver依赖，那么Spring Boot会自动选用**InfrastructureAdvisorAutoProxyCreator**；否则将会选用**AnnotationAwareAspectJAutoProxyCreator**。大家可以通过下面这种方式来验证三者的优先级，AnnotationAwareAspectJAutoProxyCreator是优先级最高、最通用的一个。
```java
public class AutoProxyCreatorPriorityApplication {
    public static final String AUTO_PROXY_CREATOR_BEAN_NAME =
            "org.springframework.aop.config.internalAutoProxyCreator";
    public static void main(String[] args) {
        // STEP 1：构造 BeanDefinitionRegistry
        // STEP 2 3 4 依次向 BeanDefinitionRegistry 中注册三种 AbstractAdvisorAutoProxyCreator BeanDefinition 实例，
        // 但 name 完全一致，即 AUTO_PROXY_CREATOR_BEAN_NAME
        BeanDefinitionRegistry beanDefinitionRegistry = new SimpleBeanDefinitionRegistry();

        // STEP 2：注册 InfrastructureAdvisorAutoProxyCreator BeanDefinition
        AopConfigUtils.registerAutoProxyCreatorIfNecessary(beanDefinitionRegistry);
        BeanDefinition infrastructureAdvisorCreatorBeanDefinition = beanDefinitionRegistry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
        System.out.println(infrastructureAdvisorCreatorBeanDefinition.getBeanClassName());

        // STEP 3：注册 AspectJAwareAdvisorAutoProxyCreator BeanDefinition
        AopConfigUtils.registerAspectJAutoProxyCreatorIfNecessary(beanDefinitionRegistry);
        BeanDefinition aspectJAwareAdvisorCreatorBeanDefinition = beanDefinitionRegistry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
        System.out.println(aspectJAwareAdvisorCreatorBeanDefinition.getBeanClassName());

        // STEP 4：注册 AnnotationAwareAspectJAutoProxyCreator BeanDefinition
        AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(beanDefinitionRegistry);
        BeanDefinition annotationAwareAspectJCreatorBeanDefinition = beanDefinitionRegistry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
        System.out.println(annotationAwareAspectJCreatorBeanDefinition.getBeanClassName());
    }
}
```
`InfrastructureAdvisorAutoProxyCreator`是如何判断是否需要为当前Bean创建代理对象的呢？

1. 首先，它会从Spring IoC容器中一次性获取所有Advisor；
2. 然后，逐一遍历每个Advisor，若当前Advisor所对应的BeanDefinition的Role等于`BeanDefinition.ROLE_INFRASTRUCTURE`，那么该Advisor就具备候选资质；
3. 最后，从具备候选资质的Advisor列表中选取与当前Bean匹配的Advisor，如果最终存在相匹配的Advisor，那么就为当前Bean创建代理对象；那么是如何裁定是否匹配的呢？若该Advisor是PointcutAdvisor类型，那么就根据ClassFilter与MethodMatcher去匹配当前Bean；若该Advisor是IntroductionAdvisor类型，那么就根据ClassFilter去匹配当前Bean。

`AnnotationAwareAspectJAutoProxyCreator`又是如何判断是否需要为当前Bean创建代理对象的呢？这部分逻辑比较复杂，如果想了解详细逻辑，参见笔者之前写的一篇文章《》。

1. 首先，它会从Spring IoC容器中一次性获取所有Advisor (一般，这些Advisor是用户或者开源组件中自定义的)，默认这些Advisor具备候选资质，压根不用像InfrastructureAdvisorAutoProxyCreator那样还要具体判断是否具备候选资质，这也从侧面说明：为什么AnnotationAwareAspectJAutoProxyCreator比InfrastructureAdvisorAutoProxyCreator优先级更高；
2. 然后，它再从Spring IoC容器中获取所有由`@Aspect`标注的Bean，将这些切面Bean中由@Before、@After和@Around等标注的方法封装成一个PointcutAdvisor列表，至此将步骤一和步骤二中的Advisor组合为一个候选Advisor列表；
3. 最后，从具备候选资质的Advisor列表中选取与当前Bean匹配的Advisor，如果最终存在相匹配的Advisor，那么就为当前Bean创建代理对象；那么是如何裁定是否匹配的呢？若该Advisor是PointcutAdvisor类型，那么就根据ClassFilter与MethodMatcher去匹配当前Bean；若该Advisor是IntroductionAdvisor类型，那么就根据ClassFilter去匹配当前Bean。

> Spring AOP之所以能支持以Aspectj注解风格去定义切面，靠的就是AnnotationAwareAspectJAutoProxyCreator！


代理对象的创建规则已经清晰了，接下来就要搞清楚究竟是如何创建代理对象的。Spring AOP依托**JDK动态代理**和**CGLIB代理**技术来创建代理对象，关于这方面的知识参见笔者之前写的一篇文章《Java动态代理》，这里就不再赘述了。

理论知识基本介绍完毕，下面进入实战环节。摆在大家面前的第一道坎应该是选取合适的Advisor，究竟是PointcutAdvisor还是IntroductionAdvisor呢？PointcutAdvisor持有一个Advice和一个Pointcut，Spring AOP 将Advice建模为`org.aopalliance.intercept.MethodInterctptor`拦截器，Pointcut用于声明应该在哪些Joinpoint (连接点) 处应用切面逻辑，而Joinpoint在SpringAOP 中专指方法的执行，因此，PointcutAdvisor中的Advice是方法级的拦截器；IntroductionAdvisor仅持有一个Advice和一个ClassFilter，显然，IntroductionAdvisor中的Advice是类级的拦截器。如果选用IntroductionAdvisor，可我们无法知道哪些类需要拦截啊，相反，如果选用PointcutAdvisor，那就可以借助MethodMatcher中的matches()方法准确拦截持有`@OperationLog`注解的目标方法。既然认准了PointcutAdvisor，那既可以直接实现PointcutAdvisor接口，也可以继承AbstractPointcutAdvisor，还可以继承AbstractBeanFactoryPointcutAdvisor，怎么搞都行，只要能包住Advice和Pointcut就行。如下所示：
```java
public class OperationLogPointcutAdvisor extends AbstractBeanFactoryPointcutAdvisor {
    private Pointcut pointcut;

    public OperationLogPointcutAdvisor() {
    }

    /**
     * @param pointcut
     * @param advice
     */
    public OperationLogPointcutAdvisor(Pointcut pointcut, Advice advice) {
        this.pointcut = pointcut;
        setAdvice(advice);
    }

    public void setPointcut(Pointcut pointcut) {
        this.pointcut = pointcut;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }
}
```

那么，对于Pointcut又该如何取舍呢？我们期待它的ClassFiler能够匹配所有类，而它的MethodMatcher只需要静态匹配就好了，即MethodMatcher中的`isRuntime()`方法返回`false`，这么一合计，`StaticMethodMatcherPointcut`简直完美契合。如下所示：
```java
public class OperationLogPointcut extends StaticMethodMatcherPointcut {
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        Annotation operationLogAnnotation = AnnotationUtils.findAnnotation(method, OperationLog.class);
        return Objects.nonNull(operationLogAnnotation);
    }
}
```

对于Advice呢，直接使用强大的`org.aopalliance.intercept.MethodInterceptor`接口即可，它可以模拟实现`MethodBeforeAdvice`、`AfterReturningAdvice`和`ThrowsAdvice`等。如下所示：
```java
public class OperationLogInterceptor implements MethodInterceptor {

    private OperatorService operatorService;

    private LogRecordPersistenceService logRecordPersistenceService;

    private final BizNoParser bizNoParser;

    private final RequestMappingParser requestMappingParser;

    public OperationLogInterceptor() {
        this.bizNoParser = new BizNoParser();
        this.requestMappingParser = new RequestMappingParser();
    }

    /**
     * @param invocation  连接点，Spring AOP 在连接点周围维护了拦截器链
     * @return            返回目标方法执行的结果
     * @throws Throwable  目标方法执行过程中所抛出的异常
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = null;
        Throwable throwable = null;
        try {
            result = invocation.proceed();
        } catch (Throwable e) {
            throwable = e;
        }

        // 切面逻辑
        persistOperationLog(new MethodInvocationResult(invocation, result, throwable));

        // 如果目标方法执行过程中抛出了异常，那么一定要重新抛出
        if (Objects.nonNull(throwable)) {
            throw throwable;
        }

        return result;
    }

    public void setOperatorService(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    public void setLogRecordPersistenceService(LogRecordPersistenceService logRecordPersistenceService) {
        this.logRecordPersistenceService = logRecordPersistenceService;
    }

    // +------------------------------------------------+
    // |               private methods                  |
    // +------------------------------------------------+

    private void persistOperationLog(MethodInvocationResult methodInvocationResult) {
        LogRecord logRecord = encapsulateLogRecord(methodInvocationResult);
        logRecordPersistenceService.doLogRecordPersistence(logRecord);
    }

    private LogRecord encapsulateLogRecord(MethodInvocationResult methodInvocationResult) {
        MethodInvocation methodInvocation = methodInvocationResult.getMethodInvocation();
        Method method = methodInvocation.getMethod();
        Operator operator = getOperator();
        Object result = methodInvocationResult.getResult();

        Map<String, Object> operationLogAnnotationAttrMap = getOperationLogAnnotationAttr(method);
        String requestMapping = requestMappingParser.parse(method);
        BizCategory bizCategory =  (BizCategory) operationLogAnnotationAttrMap.get("bizCategory");
        String bizTarget =  (String) operationLogAnnotationAttrMap.get("bizTarget");
        String originBizNo = (String) operationLogAnnotationAttrMap.get("bizNo");
        String bizNo =  bizNoParser.parse(new BizNoParseInfo(methodInvocation, result, originBizNo));

        return LogRecord.builder()
                .withOperatorId(operator.getOperatorId())
                .withOperatorName(operator.getOperatorName())
                .withOperationTarget(bizTarget)
                .withOperationCategory(bizCategory)
                .withBizNo(bizNo)
                .withRequestMapping(requestMapping)
                .withOperationResult(Objects.isNull(methodInvocationResult.getThrowable()))
                .withOperationTime(LocalDateTime.now())
                .build();
    }

    private Map<String, Object> getOperationLogAnnotationAttr(Method method) {
        Annotation operationLogAnnotation = AnnotationUtils.findAnnotation(method, OperationLog.class);
        return AnnotationUtils.getAnnotationAttributes(operationLogAnnotation);
    }

    private Operator getOperator() {
        return Optional.ofNullable(operatorService.getOperator())
                .orElse(new Operator());
    }
}
```
万事俱备，将`OperationLogPointcutAdvisor`声明为一个Bean吧，但千万别遗漏了这么一行：`@Role(BeanDefinition.ROLE_INFRASTRUCTURE)`。为什么强烈建议加这一行代码呢？当作思考题吧！
