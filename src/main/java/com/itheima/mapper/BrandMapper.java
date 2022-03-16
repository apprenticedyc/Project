package com.itheima.mapper;

import com.itheima.pojo.Brand;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface BrandMapper {
    /**
     * 查询所有
     * @return
     */
    @Select("select * from tb_brand")
    @ResultMap("brandResultMap")
    List<Brand> selectAll();


    /**
     * 添加数据
     * @param brand
     */
    @Insert("insert into tb_brand values(null,#{brandName},#{companyName},#{ordered},#{description},#{status})")
   void add(Brand brand);

    /**
     * 完成批量删除
     * @param ids
     */
    void deleteByIds(@Param("ids") int[] ids);


    /**
     *查询所有记录数
     */
    @Select("select count(*)from tb_brand")
    int selectTotalCount();


    /**
     * 查询当前页面数据返回一个List集合
     */
    @Select(" select * from tb_brand limit #{begin} , #{size}")
    @ResultMap("brandResultMap")
 List<Brand> selectByPage(@Param("begin") int begin,@Param("size") int size);



    /**
     *根据条件查询满足条件的所有记录数
     */
    int selectTotalCountByCondition(Brand brand);



    /**
     * 根据条件和当前所在页进行的查询
     * @param begin
     * @param size
     * @param brand
     * @return
     */
    List<Brand> selectByPageAndCondition(@Param("begin") int begin,@Param("size") int size,@Param("brand")Brand brand);

}
