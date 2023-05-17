package io.github.dk900912.oplog.context;

import io.github.dk900912.oplog.support.AttributeAccessorSupport;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author dukui
 */
public final class OperationLogContextSupport extends AttributeAccessorSupport implements OperationLogContext {

    private final OperationLogContext parent;

    public OperationLogContextSupport(OperationLogContext parent) {
        super();
        this.parent = parent;
    }

    @Override
    public OperationLogContext getParent() {
        return parent;
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(", ", "OperationLogContextSupport[", "]")
                .add("id='" + hashCode() + "'")
                .add("parent='" + (Objects.isNull(this.getParent()) ? 0 : this.getParent().hashCode()) + "'");
        for (String attributeName : attributeNames()) {
            if (PREVIOUS_CONTENT.equals(attributeName)) {
                continue;
            }
            Object attributeValue = getAttribute(attributeName);
            stringJoiner.add(attributeName + "='" + attributeValue + "'");
        }
        return stringJoiner.toString();
    }

}