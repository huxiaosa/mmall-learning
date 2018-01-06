package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
/**
 * 封装响应对象为字节,持久化对象(返回对象的成员变量)
 * 1.私有构造方法
 * 2.开放成员变量的get方法，以便于持久化对象获取数据
 * 3.封装public方法(返回为ServerResponse<T>的持久化对象)
 * 4.@JsonIgnore不添加至Json
 * 5.NON_NULL 保证序列化json的时候，若为null的对象，key消失(成员变量为空,则不序列化)
 * Created by huxiaosa on 2017/12/16.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
    private int status;
    private String msg;
    private T data;
    private  ServerResponse(int status){
       this.status=status;
    }
    private  ServerResponse(int status,T data){
        this.status=status;
        this.data=data;
    }
    private  ServerResponse(int status,String msg,T data){
        this.status=status;
        this.data=data;
        this.msg=msg;
    }
    private  ServerResponse(int status,String msg){
        this.status=status;
        this.msg=msg;
    }
    @JsonIgnore
    public boolean isSuccess(){
        return this.status==ResponseCode.SUCCESS.getCode();
    }
    public int getStatus(){
        return status;
    }
    public T getData(){
        return data;
    }
    public String getMsg(){
        return msg;
    }
   public static <T> ServerResponse<T> createBySuccess(){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
   }
    public static <T> ServerResponse<T> createBySuccessMessage(String msg){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
    public static <T> ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }
    public static <T> ServerResponse<T> createBySuccess(String msg,T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }
    public static <T> ServerResponse<T> createByError(){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }
    public static <T> ServerResponse<T> createByErrorMessage(String errorMsg){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),errorMsg);
    }
    public static  <T> ServerResponse<T> createByErrorCodeMsg(int errorCode,String errorMsg ){
        return new ServerResponse<T>(errorCode,errorMsg);
    }
}
