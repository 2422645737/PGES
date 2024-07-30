package org.example.pges.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pges.entity.dto.SearchParamDTO;
import org.example.pges.entity.po.BusinessPO;
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

    int hasIndex(@Param("word") String word,@Param("code") String code);

    /**
     * 通过code和word检索id集合
     * @param word
     * @param code
     * @return {@link List }<{@link Long }>
     */

    Object getIdsByWordAndCode(@Param("word") String word, @Param("code") String code);

    /**
     * 通过code和word检索出所有项目
     * @param word
     * @param code
     * @return {@link List }<{@link Long }>
     */

    List<ESIndexPo> getByWordAndCode(@Param("word") String word, @Param("code") String code);
    /**
     * 更新索引
     * @param word
     * @param ids
     * @param code
     * @return int
     */

    int updateIndex(@Param("word") String word,@Param("ids") Long[] ids,@Param("code") String code);

    /**
     * 插入索引
     * @param esIndexPo
     * @return int
     */

    int insertIndex(@Param("esIndexPo") ESIndexPo esIndexPo);

    /**
     * 按照关键词检索
     * @param searchParamDTO
     * @return {@link Object }
     */

    List<Object> searchByParam(@Param("param")SearchParamDTO searchParamDTO);
}