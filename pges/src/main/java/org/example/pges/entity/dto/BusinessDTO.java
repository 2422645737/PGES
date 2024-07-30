package org.example.pges.entity.dto;

import com.baomidou.mybatisplus.annotation.TableId;
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
public class BusinessDTO {

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