package com.itheima.service;

import com.itheima.pojo.Brand;
import com.itheima.pojo.PageBean;
import org.apache.ibatis.annotations.Param;


import java.util.List;

public interface BrandService {

    /**
     * 查询所有
     * @return
     */
    List<Brand> selectAll();

    /**
     * 添加品牌
     * @param brand
     */
   void add(Brand brand);

    /**
     * 完成批量删除
     * @param ids
     */
    void deleteByIds( int[] ids);

    /**
     * 完成查询当前页面数据
     * @return
     */
    PageBean<Brand> selectByPage(int currentPage,int pageSize);


    /**
     * 条件查询
     * @param currentPage
     * @param pageSize
     * @param brand
     * @return
     */
   PageBean<Brand> selectByPageAndCondition(int currentPage,int pageSize,Brand brand);

}
