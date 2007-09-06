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
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Implementation of {@code Multimap} whose keys and values are ordered by their
 * natural ordering or by supplied comparators. When constructing a {@code
 * TreeMultimap}, you may provide a {@link Comparator} for the keys, a {@code
 * Comparator} for the values, both, or neither. If the keys or values lack an
 * explicit {@code Comparator}, their natural ordering is used.
 *
 * <p>The collections returned by {@code keySet}, {@code keys}, and {@code
 * asMap} iterate through the keys according to the key comparator ordering or
 * the natural ordering of the keys. Similarly, {@code get}, {@code removeAll},
 * and {@code replaceValues} return collections that iterate through the values
 * according to the value comparator ordering or the natural ordering of the
 * values. The collections generated by {@code entries} and {@code values}
 * iterate across the keys according to the above key ordering, and for each
 * key they iterate across the values according to the value ordering.
 *
 * <p>The multimap does not store duplicate key-value pairs. Adding a new
 * key-value pair equal to an existing key-value pair has no effect.
 *
 * <p>Depending on the comparators, null keys and values may or may not be
 * supported. The natural ordering does not support nulls. All optional multimap
 * methods are supported, and all returned views are modifiable.
 *
 * <p>This class is not threadsafe when any concurrent operations update the
 * multimap. Concurrent read operations will work correctly. To allow concurrent
 * update operations, wrap your multimap with a call to {@link
 * Multimaps#synchronizedSortedSetMultimap}.
 *
 * @author Jared Levy
 */
public final class TreeMultimap<K, V> extends StandardSortedSetMultimap<K, V> {
  private final Comparator<? super K> keyComparator;
  private final Comparator<? super V> valueComparator;

  /**
   * Constructs an empty {@code TreeMultimap} using the natural ordering of the
   * key and value classes. The key and value classes must satisfy the {@link
   * Comparable} interface.
   */
  public TreeMultimap() {
    this(null, null);
  }

  /**
   * Constructs an empty {@code TreeMultimap} with explicit comparators.
   *
   * @param keyComparator the comparator that determines key ordering, or
   *     {@code null} for natural ordering
   * @param valueComparator the comparator that determines value ordering, or
   *     {@code null} for natural ordering
   */
  public TreeMultimap(@Nullable Comparator<? super K> keyComparator,
      @Nullable Comparator<? super V> valueComparator) {
    super((keyComparator == null)
        ? new TreeMap<K, Collection<V>>()
        : new TreeMap<K, Collection<V>>(keyComparator));
    this.keyComparator = keyComparator;
    this.valueComparator = valueComparator;
  }

  /**
   * Constructs a {@code TreeMultimap} with the same mappings as the specified
   * {@code Multimap}.
   *
   * <p>If the supplied multimap is an instance of {@code TreeMultimap}, the
   * supplied multimap's comparators are copied to the new instance.
   *
   * <p>If the supplied multimap is not an instance of {@code TreeMultimap}, the
   * new multimap is ordered using the natural ordering of the key and value
   * classes. In that case, the key and value classes must satisfy the
   * {@link Comparable} interface.
   *
   * @param multimap the multimap whose contents are copied to this multimap
   * @see #putAll(Multimap)
   */
  @SuppressWarnings("unchecked")
  public TreeMultimap(Multimap<? extends K, ? extends V> multimap) {
    this((Objects.nonNull(multimap) instanceof TreeMultimap<?, ?>)
            ? ((TreeMultimap<K, V>) multimap).keyComparator : null,
        (multimap instanceof TreeMultimap<?, ?>)
            ? ((TreeMultimap<K, V>) multimap).valueComparator : null,
        multimap);
  }

  /**
   * Constructs a {@code TreeMultimap} with the same mappings as the specified
   * {@code Multimap}. The multimap is ordered according to the supplied {@code
   * keyComparator} and {@code valueComparator}.
   *
   * @param keyComparator the comparator that determines key ordering, or
   *     {@code null} for natural ordering
   * @param valueComparator the comparator that determines value ordering, or
   *     {@code null} for natural ordering
   * @param multimap the multimap whose contents are copied to this multimap
   * @see #putAll(Multimap)
   */
  public TreeMultimap(@Nullable Comparator<? super K> keyComparator,
      @Nullable Comparator<? super V> valueComparator,
      Multimap<? extends K, ? extends V> multimap) {
    this(keyComparator, valueComparator);
    putAll(Objects.nonNull(multimap));
  }

  /**
   * {@inheritDoc}
   *
   * <p>Creates an empty {@code TreeSet} for a collection of values for one key.
   *
   * @return a new {@code TreeSet} containing a collection of values for one
   *     key
   */
  SortedSet<V> createCollection() {
    return (valueComparator == null)
        ? new TreeSet<V>() : new TreeSet<V>(valueComparator);
  }

  /**
   * Returns the comparator that orders the multimap keys, or {@code null} if
   * natural ordering is used.
   */
  public Comparator<? super K> keyComparator() {
    return keyComparator;
  }

  /**
   * Returns the comparator that orders the multimap values, or {@code null} if
   * natural ordering is used.
   */
  public Comparator<? super V> valueComparator() {
    return valueComparator;
  }

  /**
   * {@inheritDoc}
   *
   * <p>In SortedSetMultimap, the return type of this method is narrowed from
   * {@link Set} to {@code SortedSet}.
   */
  @Override public SortedSet<K> keySet() {
    return (SortedSet<K>) super.keySet();
  }

  private static final long serialVersionUID = 4309375142408689415L;
}
