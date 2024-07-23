package org.example.pges.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pges.entity.po.BusinessPO;

import java.util.List;

@Mapper
public interface BusinessMapper extends BaseMapper<BusinessPO> {
    List<BusinessPO> findByOffset(@Param("limit") int limit, @Param("offset") int offset);
}