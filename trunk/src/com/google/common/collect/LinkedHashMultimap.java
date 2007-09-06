/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import com.google.common.base.Nullable;
import com.google.common.base.Objects;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@code Multimap} that does not allow duplicate key-value
 * entries and that returns collections whose iterators follow the ordering in
 * which the data was added to the multimap.
 *
 * <p>The collections returned by {@code keySet}, {@code keys}, and {@code
 * asMap} iterate through the keys in the order they were first added to the
 * multimap. Similarly, {@code get}, {@code removeAll}, and {@code
 * replaceValues} return collections that iterate through the values in the
 * order they were added. The collections generated by {@code entries} and
 * {@code values} iterate across the key-value mappings in the order they were
 * added to the multimap.
 *
 * <p>The iteration ordering of the collections generated by {@code keySet},
 * {@code keys}, and {@code asMap} has a few subtleties. As long as the set of
 * keys remains unchanged, adding or removing mappings does not affect the key
 * iteration order. However, if you remove all values associated with a key and
 * then add the key back to the multimap, that key will come last in the key
 * iteration order.
 *
 * <p>The multimap does not store duplicate key-value pairs. Adding a new
 * key-value pair equal to an existing key-value pair has no effect.
 *
 * <p>Keys and values may be null. All optional multimap methods are supported,
 * and all returned views are modifiable.
 *
 * <p>This class is not threadsafe when any concurrent operations update the
 * multimap. Concurrent read operations will work correctly. To allow concurrent
 * update operations, wrap your multimap with a call to {@link
 * Multimaps#synchronizedSetMultimap}.
 *
 * @author Jared Levy
 */
public final class LinkedHashMultimap<K, V> extends StandardSetMultimap<K, V> {
  /**
   * Map entries with an iteration order corresponding to the order in which the
   * key-value pairs were added to the multimap.
   */
  private final Collection<Map.Entry<K, V>> linkedEntries
      = Sets.newLinkedHashSet();

  /** Constructs an empty {@code LinkedHashMultimap}. */
  public LinkedHashMultimap() {
    super(new LinkedHashMap<K, Collection<V>>());
  }

  /**
   * Constructs a {@code LinkedHashMultimap} with the same mappings as the
   * specified {@code Multimap}. The input ordering in the constructed multimap
   * corresponds to {@link Multimap#entries()} of the input multimap. If a
   * key-value mapping appears multiple times in the input multimap, it only
   * appears once in the constructed multimap.
   */
  public LinkedHashMultimap(Multimap<? extends K, ? extends V> multimap) {
    this();
    putAll(Objects.nonNull(multimap));
  }

  /**
   * {@inheritDoc}
   *
   * <p>Creates an empty {@code LinkedHashSet} for a collection of values for
   * one key.
   *
   * @return a new {@code LinkedHashSet} containing a collection of values for
   *     one key
   */
  Set<V> createCollection() {
    return new LinkedHashSet<V>();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Creates a decorated {@code LinkedHashSet} that also keeps track of the
   * order in which key-value pairs are added to the multimap.
   *
   * @param key key to associate with values in the collection
   * @return a new decorated {@code LinkedHashSet} containing a collection of
   *     values for one key
   */
  @Override Collection<V> createCollection(@Nullable K key) {
    return new SetDecorator(key, createCollection());
  }

  private class SetDecorator extends ForwardingSet<V> {
    final K key;

    SetDecorator(K key, Set<V> delegate) {
      super(delegate);
      this.key = key;
    }

    @SuppressWarnings("unchecked")
    <E> Map.Entry<K, E> createEntry(@Nullable E value) {
      return Maps.immutableEntry(key, value);
    }

    <E> Collection<Map.Entry<K, E>> createEntries(Collection<E> values) {
      // converts a collection of values into a list of key/value map entries
      Collection<Map.Entry<K, E>> entries
          = Lists.newArrayListWithCapacity(values.size());
      for (E value : values) {
        entries.add(createEntry(value));
      }
      return entries;
    }

    void assertStateConsistency(boolean value) {
      if (!value) {
        throw new IllegalStateException(
            "LinkedHashMultimap entries are inconsistent");
      }
    }

    @Override public boolean add(@Nullable V value) {
      boolean changed = super.add(value);
      if (changed) {
        assertStateConsistency(linkedEntries.add(createEntry(value)));
      }
      return changed;
    }

    @Override public boolean addAll(Collection<? extends V> values) {
      boolean changed = super.addAll(values);
      if (changed) {
        assertStateConsistency(linkedEntries.addAll(createEntries(delegate())));
      }
      return changed;
    }

    @Override public void clear() {
      linkedEntries.removeAll(createEntries(delegate()));
      super.clear();
    }

    @Override public Iterator<V> iterator() {
      final Iterator<V> delegateIterator = super.iterator();
      return new Iterator<V>() {
        V value;

        public boolean hasNext() {
          return delegateIterator.hasNext();
        }
        public V next() {
          value = delegateIterator.next();
          return value;
        }
        public void remove() {
          delegateIterator.remove();
          linkedEntries.remove(createEntry(value));
        }
      };
    }

    @Override public boolean remove(@Nullable Object value) {
      boolean changed = super.remove(value);
      if (changed) {
        /*
         * linkedEntries.remove() will return false when this method is called
         * by entries().iterator().remove()
         */
        linkedEntries.remove(createEntry(value));
      }
      return changed;
    }

    @Override public boolean removeAll(Collection<?> values) {
      boolean changed = super.removeAll(values);
      if (changed) {
        assertStateConsistency(linkedEntries.removeAll(createEntries(values)));
      }
      return changed;
    }

    @Override public boolean retainAll(Collection<?> values) {
      /*
       * Calling linkedEntries.retainAll() would incorrectly remove values
       * with other keys.
       */
      boolean changed = false;
      Iterator<V> iterator = super.iterator();
      while (iterator.hasNext()) {
        V value = iterator.next();
        if (!values.contains(value)) {
          iterator.remove();
          linkedEntries.remove(Maps.immutableEntry(key, value));
          changed = true;
        }
      }
      return changed;
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Generates an iterator across map entries that follows the ordering in
   * which the key-value pairs were added to the multimap.
   *
   * @return a key-value iterator with the correct ordering
   */
  @Override Iterator<Map.Entry<K, V>> createEntryIterator() {
    final Iterator<Map.Entry<K, V>> delegateIterator = linkedEntries.iterator();

    return new Iterator<Map.Entry<K, V>>() {
      Map.Entry<K, V> entry;

      public boolean hasNext() {
        return delegateIterator.hasNext();
      }

      public Map.Entry<K, V> next() {
        entry = delegateIterator.next();
        return entry;
      }

      public void remove() {
        // Remove from iterator first to keep iterator valid. 
        delegateIterator.remove();
        LinkedHashMultimap.this.remove(entry.getKey(), entry.getValue());
      }
    };
  }

  /**
   * {@inheritDoc}
   *
   * <p>If {@code values} is not empty and the multimap already contains a
   * mapping for {@code key}, the {@code keySet()} ordering is unchanged.
   * However, the provided values always come last in the {@link #entries()} and
   * {@link #values()} iteration orderings.
   */
  @Override public Set<V> replaceValues(
      @Nullable K key, Iterable<? extends V> values) {
    return super.replaceValues(key, values);
  }

  /**
   * Returns a set of all key-value pairs. Changes to the returned set will
   * update the underlying multimap, and vice versa. The entries set does not
   * support the {@code add} or {@code addAll} operations.
   * 
   * <p>The iterator generated by the returned set traverses the entries in the
   * order they were added to the multimap.
   */
  @Override public Set<Map.Entry<K, V>> entries() {
    return super.entries();
  }

  /**
   * Returns a collection of all values in the multimap. Changes to the returned
   * collection will update the underlying multimap, and vice versa.
   *
   * <p>The iterator generated by the returned collection traverses the values
   * in the order they were added to the multimap.
   */
  @Override public Collection<V> values() {
    return super.values();
  }

  private static final long serialVersionUID = -7860829607800920333L;
}
