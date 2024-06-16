package org.example.pges.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.pges.entity.po.ESIndexPo;

import java.util.List;

@Mapper
public interface ESDao extends BaseMapper<ESIndexPo> {

    public int hasIndex(List<String> words);
}
