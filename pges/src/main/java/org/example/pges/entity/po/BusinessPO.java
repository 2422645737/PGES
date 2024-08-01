package org.example.pges.entity.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @description:
 * @fileName: BusinessPO
 * @author: wanghui
 * @createAt: 2024/07/23 04:39:14
 * @updateBy:
 * @copyright: 众阳健康
 */

@Data
@TableName("business")
public class BusinessPO {

    private long outEmrDetailId;

    @TableId
    private long outVisitRecordId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private long deptId;

    private long outEmrMainid;

    private String patName;

    private String pat_card_no;

    private int patAge;

    private String contentText;

    private String emrCode;
}