package io.github.dk900912.oplog.support.diff;

import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dukui
 */
public class DiffMapVisitor implements DiffNode.Visitor {

    private static final List<DiffNode.State> ON_ACCEPT_STATE =
            Arrays.asList(DiffNode.State.CHANGED, DiffNode.State.REMOVED, DiffNode.State.ADDED);

    private final Object base;

    private final Object modified;

    private final Map<String, Map<String, Object>> diffMap = new LinkedHashMap<>();

    public DiffMapVisitor(final Object base, final Object modified) {
        this.base = base;
        this.modified = modified;
    }

    @Override
    public void node(final DiffNode node, final Visit visit) {
        if (acceptable(node)) {
            collectDiffProperty(node, base, modified);
        }
    }

    protected boolean acceptable(final DiffNode node) {
        return !node.isRootNode() && node.hasChanges();
    }

    protected void collectDiffProperty(final DiffNode node, final Object base, final Object modified) {
        if (ON_ACCEPT_STATE.contains(node.getState())) {
            Object baseProperty = node.canonicalGet(base);
            Object modifiedProperty = node.canonicalGet(modified);
            if (node.getParentNode().isRootNode()) {
                final LinkedHashMap<String, Object> detailedDiffMap = new LinkedHashMap<>();
                detailedDiffMap.put("before", baseProperty);
                detailedDiffMap.put("after", modifiedProperty);
                diffMap.put(node.getPropertyName(), detailedDiffMap);
            }
        }
    }

    public Map<String, Map<String, Object>> getDiffMap() {
        return diffMap;
    }

}
