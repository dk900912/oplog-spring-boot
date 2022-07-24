package io.github.dk900912.oplog.service;

/**
 * @author dukui
 */
public interface PreviousContentSelector {

    String selectorName();

    Object selectPreviousContent(String bizNo);
}
