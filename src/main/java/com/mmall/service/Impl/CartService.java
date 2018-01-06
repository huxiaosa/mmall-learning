package com.mmall.service.Impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Spliterator;

/**
 * Created by huxiaosa on 2017/12/28.
 */
@Service("iCartService")
public class CartService implements ICartService{
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    public ServerResponse<CartVo> addCart(Integer userId,Integer productId,Integer count){
        if(productId == null || count ==null){
           return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
       Cart cart = cartMapper.selectByPrimaryKeyAndProductId(userId,productId);
       if(cart == null){
          cart = new Cart();
          cart.setQuantity(count);
          cart.setChecked(Const.Cart.CHECKED);
          cart.setProductId(productId);
          cart.setUserId(userId);
          cartMapper.insert(cart);
       }else{
           //该产品已在购物中，没有限制库存是否超过总数量
           count = cart.getQuantity() + count;
           cart.setQuantity(count);
           cartMapper.updateByPrimaryKeySelective(cart);
       }
       CartVo cartVo = this.getCartVoLimit(userId);
       return ServerResponse.createBySuccess(cartVo);
    }
    public ServerResponse<CartVo> updateCart(Integer userId,Integer productId,Integer count){
        if(productId == null || count ==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByPrimaryKey(productId);
        if(cart!=null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> deleteCart(Integer userId,String productIds){
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList)){
           return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserId(userId,productList);
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> listCart(Integer userId){
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }


    public ServerResponse<CartVo> selectOrUnSelectAll(Integer userId,Integer checked,Integer productId){
        cartMapper.checkedOrUncheckedProduct(userId,checked,productId);
        return this.listCart(userId);
    }


    /**
     * 重新计算并校验界限
     * 1 获取属于userId的数据条
     * 2 封装为CartProductVo对象
     * 3 最后封装为CartVo对象：userId所有的物品，以及是否均勾选，总价
     * 所有购物车List<Cart> cartList = cartMapper.selectCartByUserId(userId);
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        BigDecimal cartTotalPrice = new BigDecimal("0");
        if(CollectionUtils.isNotEmpty(cartList)){
           for(Cart cart:cartList){
               CartProductVo cartProductVo = new CartProductVo();
               cartProductVo.setId(cart.getId());
               cartProductVo.setUserId(cart.getUserId());
               cartProductVo.setProductId(cart.getProductId());
               Product product = productMapper.selectByPrimaryKey(cart.getProductId());
               if(product!=null){
                   cartProductVo.setProductName(product.getName());
                   cartProductVo.setProductMainImage(product.getMainImage());
                   cartProductVo.setProductSubtitle(product.getSubtitle());
                   cartProductVo.setProductStatus(product.getStatus());
                   cartProductVo.setProductPrice(product.getPrice());
                   cartProductVo.setProductStock(product.getStock());
                   //判定库存是否大于购物车买的数量
                   int buyLimitCount=0;
                   if(product.getStock()>=cart.getQuantity()){
                       //库存充足的时候
                       buyLimitCount = cart.getQuantity();
                       cartProductVo.setLimitQuantity(Const.Cart.LIMIT_SUCCESS);
                   }else{
                       buyLimitCount = product.getStock();
                       cartProductVo.setLimitQuantity(Const.Cart.LIMIT_FAIL);
                       //购物车中更新有效库存
                       Cart cartQuantity = new Cart();
                       cartQuantity.setId(product.getId());
                       cartQuantity.setQuantity(buyLimitCount);
                       cartMapper.updateByPrimaryKeySelective(cartQuantity);
                   }
                   cartProductVo.setQuantity(buyLimitCount);
                   //计算此物品总价
                   cartProductVo.setProductChecked(cart.getChecked());
                   cartProductVo.setProductPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity().doubleValue()));
               }
               //如果勾选，则计算至付款总价
               if(cart.getChecked() == Const.Cart.CHECKED){
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductPrice().doubleValue());
               }
               cartProductVoList.add(cartProductVo);
           }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVos(cartProductVoList);
        cartVo.setAllChecked(getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId){
       if(userId == null){
          return false;
       }
       return cartMapper.selectCartCheckedStatusByUserId(userId)==0;
    }
}
