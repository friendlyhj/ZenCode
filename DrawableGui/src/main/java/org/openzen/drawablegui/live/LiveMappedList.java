/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.live;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.listeners.ListenerList;

/**
 *
 * @author Hoofdgebruiker
 */
public class LiveMappedList<T, U> implements Closeable, LiveList<U> {
	private final ListenerList<Listener<U>> listeners = new ListenerList<>();
	private final Function<T, U> projection;
	private final List<U> mapped;
	private final ListenerHandle<Listener<T>> mappingListenerHandle;
	
	public LiveMappedList(LiveList<T> original, Function<T, U> projection) {
		this.projection = projection;
		mappingListenerHandle = original.addListener(new MappingListener());
		
		mapped = new ArrayList<>();
		for (T originalItem : original)
			mapped.add(projection.apply(originalItem));
	}
	
	@Override
	public void close() {
		mappingListenerHandle.close();
	}

	@Override
	public void add(U value) {
		throw new UnsupportedOperationException("Cannot modify a mapped list");
	}

	@Override
	public void add(int index, U value) {
		throw new UnsupportedOperationException("Cannot modify a mapped list");
	}

	@Override
	public void set(int index, U value) {
		throw new UnsupportedOperationException("Cannot modify a mapped list");
	}

	@Override
	public void remove(int index) {
		throw new UnsupportedOperationException("Cannot modify a mapped list");
	}

	@Override
	public void remove(U value) {
		throw new UnsupportedOperationException("Cannot modify a mapped list");
	}

	@Override
	public int indexOf(U value) {
		return mapped.indexOf(value);
	}
	
	@Override
	public int size() {
		return mapped.size();
	}

	@Override
	public U get(int index) {
		return mapped.get(index);
	}

	@Override
	public Iterator<U> iterator() {
		return mapped.iterator();
	}

	@Override
	public ListenerHandle<Listener<U>> addListener(Listener<U> listener) {
		return listeners.add(listener);
	}
	
	private class MappingListener implements Listener<T> {
		@Override
		public void onInserted(int index, T value) {
			U mappedValue = projection.apply(value);
			mapped.add(index, mappedValue);
			listeners.accept(listener -> listener.onInserted(index, mappedValue));
		}

		@Override
		public void onChanged(int index, T oldValue, T newValue) {
			U mappedNewValue = projection.apply(newValue);
			U mappedOldValue = mapped.set(index, mappedNewValue);
			listeners.accept(listener -> listener.onChanged(index, mappedOldValue, mappedNewValue));
		}

		@Override
		public void onRemoved(int index, T oldValue) {
			U oldMappedValue = mapped.remove(index);
			listeners.accept(listener -> listener.onRemoved(index, oldMappedValue));
		}
	}
}
