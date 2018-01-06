package com.mmall.service.Impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.OrderItemMapper;
import com.mmall.dao.OrderMapper;
import com.mmall.dao.PayInfoMapper;
import com.mmall.pojo.Order;
import com.mmall.pojo.OrderItem;
import com.mmall.pojo.PayInfo;
import com.mmall.service.OrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by huxiaosa on 2018/1/6.
 */
@Service("iOrderService")
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    private static AlipayTradeService tradeService;

    static {
        Configs.init("zfbinfo.properties");
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    /**
     *  (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线,需保证商户系统端不能重复，建议通过数据库sequence生成
     * @param orderNo
     * @param userId
     * @param path
     * @return
     */
  public ServerResponse pay(Long orderNo,Integer userId,String path){
      Map<String,String> resultMap = Maps.newHashMap();
      Order order =orderMapper.selectByUserIDAndOrderNo(userId,orderNo);
      if(order==null){
         return ServerResponse.createByErrorMessage("用户不存在该订单");
      }
      resultMap.put("orderNo",String.valueOf(order.getOrderNo()));

      String outTradeNo = order.getOrderNo().toString();
      String subject = new StringBuilder().append("happy mall扫码支付，订单号：").append(outTradeNo).toString();
      String totalAmount = order.getPayment().toString();
      String undiscountableAmount = "0";
      String sellerId = "";
      String body =  new StringBuilder().append("订单：").append(outTradeNo).append(",购买商品共").append(totalAmount).toString();
      String operatorId = "test_operator_id";
      String storeId = "test_store_id";

        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        List<OrderItem> orderItems = orderItemMapper.getByOrderNoUserId(userId,orderNo);
        for(OrderItem orderItem:orderItems){
           GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(),orderItem.getProductName(),
                   BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100)).longValue(),orderItem.getQuantity());
            goodsDetailList.add(goods);
        }

        /** 创建扫码支付请求builder，设置请求参数
         支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
        */
         AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("aliPay.callback.url"))
                .setGoodsDetailList(goodsDetailList);
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()){
            case SUCCESS:
                logger.info("支付宝预下单成功: )");
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);
                File folder = new File(path);
                if(!folder.exists()){
                   folder.setWritable(true);
                   folder.mkdirs();
                }
                String qrPath= String.format(path+"/qr-%s.png",response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(),256,qrPath);
                File targetFile = new File(path,qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix"+targetFile.getName());
                resultMap.put("qrUrl",qrUrl);
                return ServerResponse.createBySuccess(resultMap);
            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");
            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");
            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }

  }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response!= null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    public ServerResponse aliCallback(Map<String,String> params){
      Long orderNo = Long.parseLong(params.get("out_trade_no"));
      String tradeNo = params.get("trade_no");
      String tradeStatus = params.get("trade_status");
      Order order = orderMapper.selectOrderNo(orderNo);
      if(order==null){
         return ServerResponse.createByErrorMessage("非商城的订单");
      }
      if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
         return ServerResponse.createBySuccess("支付宝重复调用");
      }
      if(Const.AlipayCallback.RESPONSE_SUCCESS.equals(tradeStatus)){
          order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
          order.setStatus(Const.OrderStatusEnum.PAID.getCode());
          orderMapper.updateByPrimaryKeySelective(order);
      }
      PayInfo payInfo =new PayInfo();
      payInfo.setUserId(order.getUserId());
      payInfo.setOrderNo(order.getOrderNo());
      payInfo.setPayPlatform(1);
      payInfo.setPlatformNumber(tradeNo);
      payInfo.setPlatformStatus(tradeStatus);
      payInfoMapper.insert(payInfo);
      return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIDAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}
