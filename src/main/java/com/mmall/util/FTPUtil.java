package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by huxiaosa on 2017/12/28.
 */
public class FTPUtil {
    private static Logger logger = LoggerFactory.getLogger(FTPUtil.class);
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    private String ip;
    private String user;
    private String pwd;
    private int port;
    private FTPClient ftpClient;

    public FTPUtil(String ip,int port,String user,String pwd){
        this.ip=ip;
        this.port = port;
        this.user=user;
        this.pwd=pwd;
    }
    public static boolean uploadFile(List<File> fileList)throws IOException{
       FTPUtil ftpUtil = new FTPUtil(ftpIp,21,ftpUser,ftpPass);
       logger.info("开始上传");
       boolean result = ftpUtil.uploadFile("img",fileList);
       logger.info("开始链接FTP,上传结果:{}",result);
       return result;

    }

    /**
     * 存储文件于文件FTP中
     * ftpClient.storeFile(file.getName(),fileInputStream);
     * 注意在finally中关闭链接
     * @param remotePath
     * @param fileList
     * @return
     */
    private boolean uploadFile(String remotePath,List<File> fileList) throws IOException {
        boolean uploaded = false;
        FileInputStream fileInputStream = null;
        //链接FTP服务器
        if(connectServer(this.ip,this.port,this.user,this.pwd)){
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                for(File file:fileList){
                   fileInputStream = new FileInputStream(file);
                   ftpClient.storeFile(file.getName(),fileInputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                fileInputStream.close();
                ftpClient.disconnect();
                uploaded = true;
            }
        }
      return uploaded;

    }

    private boolean connectServer(String ip,int port,String user,String pwd){
        boolean isSuccess =false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user,pwd);
        } catch (IOException e) {
            logger.error("无法链接FTP服务器",e);
        }
        return isSuccess;
    }
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
