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
public class SelectorFactory {

    private final Map<String, Selector> selectorMap = new ConcurrentHashMap<>(32);

    public SelectorFactory(List<Selector> selectorList) {
        selectorMap.putAll(
                Optional.ofNullable(selectorList)
                        .orElse(new ArrayList<>(0))
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Selector::selectorName,
                                        selector -> selector,
                                        (selector1, selector2) -> selector1
                                )
                        ));
    }

    public Selector getSelector(String selectorName) {
        return selectorMap.get(selectorName);
    }
}
