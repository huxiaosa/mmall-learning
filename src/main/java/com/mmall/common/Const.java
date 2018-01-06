package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by huxiaosa on 2017/12/19.
 */
public class Const {
    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    /**
     * 使用接口分组，比枚举要好
     */
    public interface Role{
        int ROLE_CUSTOMER = 0;
        int ROLE_ADMINER = 1;
    }
    public interface Cart{
        int UN_CHECKED = 0;
        int CHECKED = 1; //选中状态
        String LIMIT_FAIL = "LIMIT_FAIL";
        String LIMIT_SUCCESS = "LIMIT_SUCCESS";
    }
    public interface ProductListOrderBy{
        Set<String> PRICR_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }
    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭");


        OrderStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("么有找到对应的枚举");
        }
    }
    public interface  AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }
}
