package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IUserService;
import com.mmall.service.IproductService;
import com.mmall.util.PropertiesUtil;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Properties;

/**
 * Created by huxiaosa on 2017/12/22.
 */
@Controller
@RequestMapping("/manage/product")
public class ProductManagerController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IproductService iproductService;

    @Autowired
    private IFileService iFileService;

    @RequestMapping(value = "save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product){
         User user = (User) session.getAttribute(Const.CURRENT_USER);
         if(user==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理元");
         }
        //校验是否为管理元
        ServerResponse response = iUserService.checkAdminRole(user);
        if(response.isSuccess()){
            //todo 分类逻辑
            return iproductService.saveOrUpdateProduct(product);
        }
        return ServerResponse.createByErrorMessage("没有管理员权限");
    }
    @RequestMapping(value = "set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId,Integer status){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理元");
        }
        //校验是否为管理元
        ServerResponse response = iUserService.checkAdminRole(user);
        if(response.isSuccess()){
            //todo 分类逻辑
            return iproductService.setSaleStatus(productId,status);
        }
        return ServerResponse.createByErrorMessage("没有管理员权限");
    }


    @RequestMapping(value = "detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理元");
        }
        //校验是否为管理元
        ServerResponse response = iUserService.checkAdminRole(user);
        if(response.isSuccess()){
            //todo 填充业务
           return iproductService.manageProductDetail(productId);
        }
        return ServerResponse.createByErrorMessage("没有管理员权限");
    }

    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum" ,defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理元");
        }
        //校验是否为管理元
        ServerResponse response = iUserService.checkAdminRole(user);
        if(response.isSuccess()){
            //todo 填充业务
            return iproductService.getProductList(pageNum,pageSize);
        }
        return ServerResponse.createByErrorMessage("没有管理员权限");
    }

    @RequestMapping(value = "search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session,String productName,Integer productId,@RequestParam(value = "pageNum" ,defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理元");
        }
        //校验是否为管理元
        ServerResponse response = iUserService.checkAdminRole(user);
        if(response.isSuccess()){
            return iproductService.productSearch(productName,productId,pageNum,pageSize);
        }
        return ServerResponse.createByErrorMessage("没有管理员权限");
    }

    /**
     * 上传文件至webApp下
     * 获取其真实路径request.getSession().getServletContext().getRealPath("upload");
     * 上传至FTP服务器上，返回支源名字
     * @param file
     * @param request
     * @return
     */
    @RequestMapping(value = "upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session,@RequestParam(value = "upload_file" , required = false) MultipartFile file, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理元");
        }
        //校验是否为管理元
        ServerResponse response = iUserService.checkAdminRole(user);
        if(response.isSuccess()){
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            Map fileMap = Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccess(fileMap);
        }
        return ServerResponse.createByErrorMessage("没有管理员权限");

    }
}
