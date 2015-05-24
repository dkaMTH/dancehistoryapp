package com.intotheballroom;

import javafx.collections.ModifiableObservableListBase;

import java.util.List;

/**
 * Created by Dmitry on 5/24/2015.
 */
public class BatchObservableList<T> extends ModifiableObservableListBase<T> {
    private final List<T> impl;

    public BatchObservableList(List<T> impl) {
        this.impl = impl;
    }

    public void beginBatchChange() {
        beginChange();
    }

    public void endBatchChange() {
        endChange();
    }

    @Override
    public T get(int index) {
        return impl.get(index);
    }

    @Override
    public int size() {
        return impl.size();
    }

    @Override
    protected void doAdd(int index, T element) {
        impl.add(index, element);
    }

    @Override
    protected T doSet(int index, T element) {
        return impl.set(index, element);
    }

    @Override
    protected T doRemove(int index) {
        return impl.remove(index);
    }
}
