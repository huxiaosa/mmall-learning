package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

/**
 * Created by huxiaosa on 2017/12/28.
 */
public interface ICartService {
    ServerResponse<CartVo> addCart(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo> updateCart(Integer userId,Integer productId,Integer count);
    ServerResponse<CartVo> deleteCart(Integer userId,String productIds);
    ServerResponse<CartVo> listCart(Integer userId);
    ServerResponse<CartVo> selectOrUnSelectAll(Integer userId,Integer checked,Integer productId);
}
