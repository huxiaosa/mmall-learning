package com.mmall.common;

import java.util.Map.Entry;
import java.util.HashMap;
/**
 * * LRU算法：最近最少使用--->原理：那就是利用双链表和hashMap
 * 1 当需要插入新的数据项的时候，如果新数据项在链表中存在（一般称为命中），则把该节点移到链表头部
 * 2 如果不存在，则新建一个节点，放到链表头部
 * 3   removeLast();hashMap.remove(Last.key) 若缓存满了，则把链表最后一个节点删除即可
 * 4   moveToFirst() 在访问数据的时候，如果数据项在链表中存在，则把该节点移到链表头部，否则返回-1。这样一来在链表尾部的节点就是最近最久未访问的数据项。
 * java实现LinkedList + hashMap
 * Created by huxiaosa on 2017/12/19.
 */
public class LRUCache<K,V> {
    private final int MAX_CACHE_SIZE;
    private Entry first;
    private Entry Last;
    private HashMap<K,Entry<K,V>> hashMap;

    public LRUCache(int cache_size){
      this.MAX_CACHE_SIZE = cache_size;
      hashMap = new HashMap<K,Entry<K,V>>();
    }
    public void put(K key,V value){
        Entry entry = getEntry(key);
        if(entry==null){
            if(hashMap.size()==MAX_CACHE_SIZE){
               hashMap.remove(Last.key);
               removeLast();
            }
           entry = new Entry();
           entry.key=key;
        }
        entry.value = value;
        moveToFirst(entry);
        hashMap.put(key,entry);
    }

    private Entry<K, V> getEntry(K key) {
        return hashMap.get(key);
    }

    /**
     * 则把链表最后一个节点删除即可
     * Last=Last.pre指向前一节点
     */
    private void removeLast(){
        if(Last!=null){
           Last=Last.pre;
           if(Last==null) first = null;
           else Last.next =null;
        }
    }

    /**
     * 优先判定entry==first
     * @param entry
     */
    private void moveToFirst(Entry entry){
        if (entry == first) return;
        if(entry.pre!=null) entry.pre.next = entry.next;
        if(entry.next!=null) entry.next.pre = entry.pre;
        if (entry == Last) Last = Last.pre;
        entry.next = first;
        first.pre = entry;
        first = entry;
        entry.pre = null;
    }




    class Entry<K, V> {
        public Entry pre;
        public Entry next;
        public K key;
        public V value;
    }
}
