package io.github.dk900912.oplog.support;

import org.springframework.lang.Nullable;

/**
 * @author dukui
 */
public interface AttributeAccessor {

    void setAttribute(String name, @Nullable Object value);

    Object getAttribute(String name);

    Object removeAttribute(String name);

    boolean hasAttribute(String name);

    String[] attributeNames();

}
