package org.example.pges.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.example.pges.handler.ArrayTypeHandler;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
@TableName(value = "me.es_index",autoResultMap = true)
public class ESIndexPo {
    @TableId
    private Long id;

    private String word;

    private Date beginTime;

    private Date endTime;

    private String code;

    @TableField(jdbcType = JdbcType.ARRAY,typeHandler = ArrayTypeHandler.class)
    private Long[] ids;

}