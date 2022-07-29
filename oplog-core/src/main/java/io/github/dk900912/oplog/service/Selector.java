package io.github.dk900912.oplog.service;

/**
 * @author dukui
 */
public interface Selector {

    String selectorName();

    Object select(String bizNo);
}
