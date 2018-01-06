package com.mmall.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.CategoryService;
import com.mmall.service.IproductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huxiaosa on 2017/12/22.
 */
@Service("iProductService")
public class IproductServiceImpl implements IproductService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private CategoryService iCategoryService;
    public ServerResponse saveOrUpdateProduct(Product product){
        if(product!=null){
            //保存子图不为null
            if(!StringUtils.isBlank(product.getSubImages())){
                String[] subImages=product.getSubImages().split(",");
                if(subImages.length>0){
                    product.setMainImage(subImages[0]);
                }
            }
            //更新:ID不为null 插入:ID为null(数据库自增)
            if(product.getId()!=null){
               int rowCount = productMapper.updateByPrimaryKeySelective(product);
               if(rowCount>0) return ServerResponse.createBySuccess("更新铲平成功");
               return ServerResponse.createByErrorMessage("更新铲平不成功");
            }else{
                int rowCount = productMapper.insert(product);
                if(rowCount>0) return ServerResponse.createBySuccess("插入成功");
                return ServerResponse.createBySuccess("铲平插入不成功");
            }
        }
        return ServerResponse.createByErrorMessage("参数不正确");
    }

    public ServerResponse<String> setSaleStatus(Integer productId,Integer status){
        if(productId==null ||status==null){
           return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount>0){
           return  ServerResponse.createBySuccess("修改状态成功");
        }
        return  ServerResponse.createByErrorMessage("修改状态失败");
    }

    /**
     * sample VO对象-value object  组装业务对象
     * hard pojo--->bo(business object)---->vo(view object)
     * @param productId
     * @return
     */
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId){
        if(productId==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product==null){
           return ServerResponse.createByErrorMessage("残品已下架");
        }
        ProductDetailVo productDetailVo = assembleProductVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }
    private ProductDetailVo assembleProductVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        /**
         * imageHost
         * parentCategoryId
         * updateTime
         */
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category==null){
            productDetailVo.setParentCategoryId(0); //根节点
        }else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        productDetailVo.setCreateTime(DateTimeUtil.DateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.DateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    /**
     * startPage-start
     * 填充自己的sql逻辑,PageHelper监听myBatis的Interceptor接口,添加Limit
     * pageHelper的PageInfo收尾
     * PageInfo pageInfo = new PageInfo(productList);
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> getProductList(int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListVos = Lists.newArrayList();
        for(Product product:productList){
           ProductListVo productListVo = assembleProductListVo(product);
           productListVos.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVos);
        return ServerResponse.createBySuccess(pageInfo);
    }
    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo  = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setPrice(product.getPrice());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    /**
     * 模糊查询产品
     * sql查询时用到where拼接技术
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> productSearch(String productName,Integer productId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(productName)){
           productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectListByproductNameAndId(productName,productId);
        List<ProductListVo> productListVos = Lists.newArrayList();
        for(Product product:productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVos.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVos);
        return ServerResponse.createBySuccess(pageInfo);
    }


    public ServerResponse<PageInfo>  getProductByKeyAndCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy){
        if(StringUtils.isBlank(keyword) && categoryId == null){
           return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        /**
         * 1 判定分类是否存在，不存在则返回空集合
         * 2 若存在则获取所有分类get_deep_category.do
         * 3 排序处理 order by price desc（设置pageHelper的orderBy拼接规则）
         * 4 依据所有categoryId获取所有product
         * select * from table where category_id in (item,item,item);
         */
        List<Integer> categoryList = new ArrayList<Integer>();
        if(categoryId!=null){
           Category category = categoryMapper.selectByPrimaryKey(categoryId);
           if(category==null && StringUtils.isBlank(keyword)){
               PageHelper.startPage(pageNum,pageSize);
               List<ProductListVo> productListVos = Lists.newArrayList();
               PageInfo pageInfo = new PageInfo(productListVos);
               return ServerResponse.createBySuccess(pageInfo);
           }
            categoryList = iCategoryService.selectCategoryAndChildrenById(categoryId).getData();
        }
        if(StringUtils.isNotBlank(keyword)){
           keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICR_ASC_DESC.contains(orderBy)){
               String[] orderByArray = orderBy.split("_");
               PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
            }
        }
        List<Product> productList = productMapper.selectListByKeywordAndCategoryList(StringUtils.isNotBlank(keyword)?keyword:null,categoryList.size()==0?null:categoryList);
        List<ProductListVo> productListVos = Lists.newArrayList();
        for(Product product:productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVos.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVos);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
