package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

/**
 * Created by huxiaosa on 2017/12/22.
 */
public interface IproductService {
    ServerResponse saveOrUpdateProduct(Product product);
    ServerResponse<String> setSaleStatus(Integer productId,Integer status);
    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductList(int pageNum, int pageSize);
    ServerResponse<PageInfo> productSearch(String productName,Integer productId,int pageNum,int pageSize);
    ServerResponse<PageInfo>  getProductByKeyAndCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy);
}
