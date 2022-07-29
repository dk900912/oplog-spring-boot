package io.github.dk900912.oplog.parser;

/**
 * @author dukui
 */
public interface Parser<T> {

    /**
     * @param target parsable target
     * @return the parsed value
     */
    Object parse(T target);

}
