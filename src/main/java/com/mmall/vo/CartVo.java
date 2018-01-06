package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by huxiaosa on 2017/12/28.
 */
public class CartVo {
    private List<CartProductVo> cartProductVos;
    private BigDecimal cartTotalPrice;
    private String imageHost;
    private Boolean allChecked; //是否军勾选

    public List<CartProductVo> getCartProductVos() {
        return cartProductVos;
    }

    public void setCartProductVos(List<CartProductVo> cartProductVos) {
        this.cartProductVos = cartProductVos;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }
}
