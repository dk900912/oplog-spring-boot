package io.github.dk900912.oplog.parser;

/**
 * @author dukui
 */
public interface Parser<T> {
    /**
     * 用于解析 bizNo、requestMapping
     *
     * @param target 解析目标
     * @return       解析结果
     */
    String parse(T target);
}
