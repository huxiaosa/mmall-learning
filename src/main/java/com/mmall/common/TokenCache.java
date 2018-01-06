package com.mmall.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存初始化为1000，最大为10000
 * 超过则使用最少使用算法，清除缓存
 * Created by huxiaosa on 2017/12/19.
 */
public class TokenCache {
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);
    //LRU算法
    private static LoadingCache<String,String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //key没有命中时，调用该方法
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });
    public static void setKey(String key,String value){
       localCache.put(key,value);
    }

    public static String getKey(String key){
       String value = null;
       try{
           value = localCache.get(key);
           if("null".equals(value)) return null;
       }catch (Exception e){
          logger.error("localCache get error",e);
       }
       return value;
    }
}
