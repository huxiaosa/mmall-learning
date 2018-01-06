package com.mmall.controller.protal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IproductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by huxiaosa on 2017/12/28.
 */
@Controller
@RequestMapping("/product")
public class ProductController {
  @Autowired
    private IproductService iproductService;

  @RequestMapping("list.do")
  @ResponseBody
  public ServerResponse<PageInfo> getListByKeyAndCategoryId(@RequestParam(value = "keyword",required = false)String keyword,
                                                            @RequestParam(value = "categoryId",required = false)Integer categoryId,
                                                            @RequestParam(value = "pageNum" ,defaultValue = "1") int pageNum,
                                                            @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
                                                            @RequestParam(value = "orderBy",defaultValue = "")String orderBy){

      return iproductService.getProductByKeyAndCategory(keyword,categoryId,pageNum,pageSize,orderBy);
  }
}
