package io.github.dk900912.oplog.support.diff;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author dukui
 */
public class DiffSelectorRegistry {

    private final Map<String, DiffSelectorMethod> registry = new HashMap<>();

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private DiffSelectorRegistry() {}

    public static DiffSelectorRegistry getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public DiffSelectorMethod getDiffSelectorMethod(String name) {
        acquireReadLock();
        try {
            return this.registry.get(name);
        } finally {
            releaseReadLock();
        }
    }

    public void acquireReadLock() {
        this.readWriteLock.readLock().lock();
    }

    public void releaseReadLock() {
        this.readWriteLock.readLock().unlock();
    }

    public void acquireWriteLock() {
        this.readWriteLock.writeLock().lock();
    }

    public void releaseWriteLock() {
        this.readWriteLock.writeLock().unlock();
    }

    public void register(Object bean, Method method) {
        acquireWriteLock();
        try {
            DiffSelectorMethod diffSelectorMethod = createDiffSelectorMethod(bean, method);
            if (shouldRegister(diffSelectorMethod)) {
                this.registry.put(diffSelectorMethod.getDescription(), diffSelectorMethod);
            }
        } finally {
            releaseWriteLock();
        }
    }

    private DiffSelectorMethod createDiffSelectorMethod(Object bean, Method method) {
        return new DiffSelectorMethod(bean, method);
    }

    private boolean shouldRegister(DiffSelectorMethod diffSelectorMethod) {
        return !diffSelectorMethod.isVoid();
    }

    private static class SingletonHolder {
        private static final DiffSelectorRegistry INSTANCE = new DiffSelectorRegistry();
    }

}