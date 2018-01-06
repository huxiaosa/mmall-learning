package com.mmall.service.Impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.CategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by huxiaosa on 2017/12/20.
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements CategoryService{
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    /**
     * 不需要判定parentId，只管添加即可
     */
    @Autowired
    CategoryMapper categoryMapper;
   public ServerResponse addCategory(String categoryName,Integer parentId){
       if(parentId==null|| StringUtils.isBlank(categoryName)){
          return ServerResponse.createByErrorMessage("添加品类参数错误");
       }
       Category category = new Category();
       category.setName(categoryName);
       category.setParentId(parentId);
       category.setStatus(true);
       int resultCount = categoryMapper.insert(category);
       if(resultCount>0){
          return ServerResponse.createBySuccess("添加成功");
       }
       return ServerResponse.createByErrorMessage("添加失败");
   }

    public ServerResponse updateCategoryName(String categoryName,Integer categoryId){
        if(categoryId==null|| StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(resultCount>0){
            return ServerResponse.createBySuccess("更新成功");
        }
        return ServerResponse.createByErrorMessage("更新失败");
    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到相应分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet,categoryId);
        List<Integer> list = Lists.newArrayList();
        if(categoryId!=null){
            for(Category categoryItem:categorySet){
                list.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(list);
    }

    /**
     * 递归算法，求出子节点
     * Set集合去重，重写equals与hashcode方法（依据id比较）
     * myBatis不会返回null对象，不需要空判断
     * @param categorySet
     * @param categoryId
     * @return
     */
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
      Category category = categoryMapper.selectByPrimaryKey(categoryId);
      if(category!=null){
          categorySet.add(category);
      }
        //查找子节点,退出条件for循环
      List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
      for(Category categoryItem:categoryList){
          findChildCategory(categorySet,categoryItem.getId());
      }
      return categorySet;
    }



}
