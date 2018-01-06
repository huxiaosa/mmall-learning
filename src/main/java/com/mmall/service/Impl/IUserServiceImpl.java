package com.mmall.service.Impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by huxiaosa on 2017/12/16.
 */
@Service("iUserService")
public class IUserServiceImpl implements IUserService{
    @Autowired
    private UserMapper userMapper;

    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount==0){
           return ServerResponse.createByErrorMessage("用户名不存在");
        }
        // todo 密码登陆MD5
        String md5PassWord = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5PassWord);
        if(user == null){
          return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功",user);
    }


    public ServerResponse<String> register(User user){
        ServerResponse response =this.checkValid(user.getUsername(),Const.USERNAME);
        if(!response.isSuccess()){
            return response;
        }
        response =this.checkValid(user.getEmail(),Const.EMAIL);
        if(!response.isSuccess()){
            return response;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
         int resultCount = userMapper.insert(user);
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }


    public ServerResponse<String> checkValid(String str,String type){
        if(StringUtils.isNotBlank(type)){
            //开启校验
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if(resultCount>0){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount>0){
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    public ServerResponse selectQuestion(String username){
       ServerResponse response =this.checkValid(username,Const.USERNAME);
       if(response.isSuccess()){
           //用户不存在
           return ServerResponse.createByErrorMessage("用户不存在");
       }
       String question = userMapper.selectQuestionByUsername(username);
       if(StringUtils.isNotBlank(question)){
          return ServerResponse.createBySuccess(question);
       }
       return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    /***
     * 查询答案是否正确
     * 若正确，则设置12小时的本地token
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if(resultCount>0){
          String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey("token_"+username,forgetToken);
            System.out.println("token:" +forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken) {
        if (StringUtils.isBlank(forgetToken))  return ServerResponse.createByErrorMessage("参数错误,需要传递token");
        ServerResponse response = this.checkValid(username, Const.USERNAME);
        if (response.isSuccess())  return ServerResponse.createByErrorMessage("用户不存在");
        String token = TokenCache.getKey("token_" + username);
        System.out.println("token:" +token);
        if (StringUtils.isBlank(token)) return ServerResponse.createByErrorMessage("token无效或者过期");
        if (StringUtils.equals(forgetToken,token)) {
            String md5PassWord = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, md5PassWord);
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("密码修改成功");
            }else{
                return ServerResponse.createByErrorMessage("token错误");
            }
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    public ServerResponse<String> ResetPassword(User user,String passwordOld,String passwordNew) {
       //避免横向越权，要校验一下旧密码一定是此用户(若只是检测旧密码的正确性，则有可能匹配到其他用户的权限)
        int resultCount =userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8((passwordNew)));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount>0){
           return ServerResponse.createBySuccessMessage("更新成功");
        }
        return ServerResponse.createByErrorMessage("更新失败");

    }

    public ServerResponse<User> updateUserInformation(User user){
       //email校验:新的email的存在性
        int resultCount =userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount>0){
            return ServerResponse.createByErrorMessage("email已经被别人使用");
        }
        //username不能更新
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount>0){
            return ServerResponse.createBySuccess("更新重构",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新失败");
    }

    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);

    }

    /**
     * 校验是否为管理元
     */
    public ServerResponse checkAdminRole(User user){
        if(user!=null && user.getRole()==Const.Role.ROLE_ADMINER){
           return  ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}
