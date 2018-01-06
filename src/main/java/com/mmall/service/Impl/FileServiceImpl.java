package com.mmall.service.Impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by huxiaosa on 2017/12/26.
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService{
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    /**
     * 1 获取其上传的文件名
     * 2 得到其扩展名
     * 3 UUID生成随机名
     * 4 创建文件夹
     * 5 复制文件内容file.transferTo(target);
     * @param file
     * @param path
     * @return
     */
   public String upload(MultipartFile file,String path){
     String fileName = file.getOriginalFilename();
     String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
     String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
     logger.info("开始上传文件，上传的文件名:{}，路径:{},新文件名称:{}",fileName,path,uploadFileName);
     File fileDir = new File(path);
     if(!fileDir.exists()){
         fileDir.setWritable(true);
         fileDir.mkdirs();
     }
     File target = new File(path,uploadFileName);
       try {
           file.transferTo(target);
           //将文件上传至FTP服务器上
           FTPUtil.uploadFile(Lists.newArrayList(target));
           target.delete();
       } catch (IOException e) {
           logger.error("上传失败",e);
           return null;
       }
      return target.getName();
   }
}
