package org.example.pges.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pges.entity.po.BusinessPO;

import java.util.List;

@Mapper
public interface BusinessMapper extends BaseMapper<BusinessPO> {
    /**
     * 通过偏移量获取数据
     * @param limit
     * @param offset
     * @return {@link List }<{@link BusinessPO }>
     */

    List<BusinessPO> findByOffset(@Param("limit") int limit);

    /**
     * 更新es标识
     * @param ids
     * @return int
     */
    int updateEsFlag(@Param("ids")List<Long> ids);

    /**
     * 根据病历明细id直接检索数据
     * @param ids
     * @return {@link List }<{@link BusinessPO }>
     */

    List<BusinessPO> searchByOutEmrDetailIds(List<Long> ids);

    /**
     * 根据病历明细id直接检索数据(只检索主键和创建时间，提高速度)
     * @param ids
     * @return {@link List }<{@link BusinessPO }>
     */

    List<BusinessPO> searchTimeByOutEmrDetailIds(List<Long> ids);
}