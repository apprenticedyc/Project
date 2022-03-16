package com.itheima.service.impl;

import com.itheima.mapper.BrandMapper;
import com.itheima.pojo.Brand;

import com.itheima.pojo.PageBean;
import com.itheima.service.BrandService;
import com.itheima.util.SqlSessionFactoryUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

public class BrandServiceImpl implements BrandService {
    //1. 创建SqlSessionFactory 工厂对象
    SqlSessionFactory factory = SqlSessionFactoryUtils.getSqlSessionFactory();


    @Override
    public List<Brand> selectAll() {
        //2. 获取SqlSession对象
        SqlSession sqlSession = factory.openSession();
        //3. 获取BrandMapper
        BrandMapper mapper = sqlSession.getMapper(BrandMapper.class);

        //4. 调用方法
        List<Brand> brands = mapper.selectAll();

        //5. 释放资源
        sqlSession.close();

        return brands;
    }

    @Override
    public void add(Brand brand) {
        //2. 获取SqlSession对象
        SqlSession sqlSession = factory.openSession();
        //3. 获取BrandMapper
        BrandMapper mapper = sqlSession.getMapper(BrandMapper.class);
        //4. 调用方法
        mapper.add(brand);
        //数据库信息发生变化所以要提交事务
        sqlSession.commit();
        //5. 释放资源
        sqlSession.close();
    }


    @Override
    public void deleteByIds(int[] ids) {
        //2. 获取SqlSession对象
        SqlSession sqlSession = factory.openSession();
        //3. 获取BrandMapper
        BrandMapper mapper = sqlSession.getMapper(BrandMapper.class);
        //4. 调用方法
        mapper.deleteByIds(ids);
        //数据库信息发生变化所以要提交事务
        sqlSession.commit();
        //5. 释放资源
        sqlSession.close();
    }

    @Override
    public PageBean<Brand> selectByPage(int currentPage, int pageSize) {
        //2. 获取SqlSession对象
        SqlSession sqlSession = factory.openSession();
        //3. 获取BrandMapper
        BrandMapper mapper = sqlSession.getMapper(BrandMapper.class);
        //4. 计算索引
        int begin = (currentPage - 1) * pageSize;
        //5. 调用方法 获取当前页数据集合
        List<Brand> rows = mapper.selectByPage(begin, pageSize);
        //6. 调用方法 获取总记录数
        int count = mapper.selectTotalCount();
        //7.将参数传入PageBean构造器封装成PageBean对象
        PageBean<Brand> brandPageBean = new PageBean<>();
        brandPageBean.setRows(rows);
        brandPageBean.setTotalCount(count);
        //8. 释放资源
        sqlSession.close();
        return brandPageBean;
    }


    @Override
    public PageBean<Brand> selectByPageAndCondition(int currentPage, int pageSize, Brand brand) {
        //2. 获取SqlSession对象
        SqlSession sqlSession = factory.openSession();
        //3. 获取BrandMapper
        BrandMapper mapper = sqlSession.getMapper(BrandMapper.class);
        //4. 计算索引
        int begin = (currentPage - 1) * pageSize;

        //处理前端传入的brand数据 以达到模糊查询的目的
        String brandName = brand.getBrandName();
        String companyName = brand.getCompanyName();
        if (brandName!=null) {
            brand.setBrandName("%" + brandName + "%");
        }
        if (brandName!=null) {
            brand.setCompanyName("%" + companyName + "%");
        }
        //5.调用方法 获取当前页数据集合
        List<Brand> rows = mapper.selectByPageAndCondition(begin, pageSize, brand);
        //6. 调用方法 获取总记录数
        int count = mapper.selectTotalCountByCondition(brand);
        //7.将参数传入PageBean构造器封装成PageBean对象
        PageBean<Brand> brandPageBean = new PageBean<>();
        brandPageBean.setRows(rows);
        brandPageBean.setTotalCount(count);
        //8. 释放资源
        sqlSession.close();
        return brandPageBean;
    }
}
