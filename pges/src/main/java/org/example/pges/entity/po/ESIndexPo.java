package org.example.pges.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("business")
public class ESIndexPo {
    private String word;
}