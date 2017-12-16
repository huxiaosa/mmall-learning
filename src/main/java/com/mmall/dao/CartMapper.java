package com.mmall.dao;

import com.mmall.pojo.Cart;

public interface CartMapper {
    /***
     * insert为完全插入
     * insertSelective优先判定是否为空
     * @param id
     * @return
     */
    int deleteByPrimaryKey(Integer id);
    int insert(Cart record);
    int insertSelective(Cart record);
    Cart selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(Cart record);
    int updateByPrimaryKey(Cart record);
}