package org.example.pges.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.pges.entity.po.ESIndexPo;

import java.util.List;

/**
 * @description:
 * @fileName: ESDao
 * @author: wanghui
 * @createAt: 2024/07/23 09:26:45
 * @updateBy:
 * @copyright: 众阳健康
 */

@Mapper
public interface ESMapper extends BaseMapper<ESIndexPo> {

    /**
     * 判断是否包含单词的index
     * @param words
     * @return int
     */

    int hasIndex(List<String> words);
}