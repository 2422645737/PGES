package org.example.pges.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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

    private Long outEmrDetailId;

    private Long outVisitRecordId;

    private Date createTime;

    private Long deptId;

    private Long outEmrMainid;

    private String patName;

    private String pat_card_no;

    private Integer patAge;

    private String contentText;

    private String emrCode;
}