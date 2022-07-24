package io.github.dk900912.oplog.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author dukui
 */
public class PreviousContentSelectorFactory {

    private final Map<String, PreviousContentSelector> selectorMap = new ConcurrentHashMap<>(32);

    public PreviousContentSelectorFactory(List<PreviousContentSelector> selectorList) {
        selectorMap.putAll(
                Optional.ofNullable(selectorList)
                        .orElse(new ArrayList<>(0))
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        PreviousContentSelector::selectorName,
                                        selector -> selector,
                                        (selector1, selector2) -> selector1
                                )
                        ));
    }

    public PreviousContentSelector getSelector(String selectorName) {
        return selectorMap.get(selectorName);
    }
}
