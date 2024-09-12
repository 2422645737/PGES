package org.example.pges.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pges.entity.dto.SearchParam;
import org.example.pges.entity.po.Document;
import org.example.pges.entity.po.ESIndex;

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
public interface ESMapper extends BaseMapper<ESIndex> {

    /**
     * 判断是否包含单词的index
     * @param word
     * @param code
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

    List<ESIndex> getByWordAndCode(@Param("word") String word, @Param("code") String code);

    /**
     * 更新索引
     * @param esIndex
     * @return int
     */

    int updateIndex(@Param("esIndex") ESIndex esIndex);

    /**
     * 插入索引
     * @param esIndex
     * @return int
     */

    int insertIndex(@Param("esIndexPos") List<ESIndex> esIndex);

    /**
     * 按照关键词检索
     * @param searchParam
     * @return {@link Object }
     */

    List<Document> searchByParam(@Param("param") SearchParam searchParam);

    /**
     * 查询 出现次数 > 3 and 最大id数量 <= 1000 的word，对于此类word，可以对其进行合并
     * @param minSize
     * @return {@link List }<{@link String }>
     */

    List<String> getLeastIndex(Integer minSize);

    /**
     * 获取id数量过多的索引
     * @param maxSize
     * @return {@link List }<{@link ESIndex }>
     */

    List<Long> getMostIndex(Integer maxSize);
}