/*
 * 
 */
package com.zimbra.cs.account.cache;

import java.util.List;

import com.zimbra.cs.account.NamedEntry;

public interface INamedEntryCache<E extends NamedEntry> extends IEntryCache {
    public void clear();
    public void remove(String name, String id);
    public void remove(E entry);
    public void put(E entry);
    public void replace(E entry);
    public void put(List<E> entries, boolean clear);
    public E getById(String key);
    public E getByName(String key);
}
