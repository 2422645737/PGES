package org.example.pges.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("es_index")
public class ESIndexPo {
    @TableId
    private String word;

    private Date beginTime;

    private Date endTime;

    private String code;

    private Long[] ids;
}