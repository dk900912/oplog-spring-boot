package io.github.dk900912.oplog.service;

/**
 * <h3> 目标方法执行结果分析器 </h3>
 *
 * @author dukui
 */
public interface OperationResultAnalyzerService {

    /**
     * <p>目标方法如果执行失败，那么建议直接抛出异常，然后由 <b>Spring MVC</b> 统一异常处理机制来应对异常；
     * 但有些开发人员喜欢在 <b>Controller</b> 中对业务异常进行抑制，然后将统一响应体响应给调用方，这样做会
     * 导致大量构建响应体的代码分散在 <b>Controller</b> 的方法内，同时也使得本组件对目标方法执行结果的检测
     * 不够准确。基于这一事实，{@link OperationResultAnalyzerService} 拓展点也就有必要放出来了。</p>
     *
     * @param throwable exception threw by target method
     * @param result    the execution result of target method
     * @return          true - success, false - failure
     */
    boolean analyzeOperationResult(Throwable throwable, Object result);
}
