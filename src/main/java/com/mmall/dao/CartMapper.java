package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
    Cart selectByPrimaryKeyAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);
    List<Cart> selectCartByUserId(Integer userId);
    int selectCartCheckedStatusByUserId(Integer userId);
    int deleteByUserId(@Param("userId") Integer userId,@Param("productList") List<String> productList);
    int checkedOrUncheckedProduct(@Param("userId")Integer userId,@Param("checked") Integer checked,@Param("productId") Integer productId);
}