package org.example.pges.entity.po;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @fileName: ItemNode
 * @author: wanghui
 * @createAt: 2024/09/12 10:58:14
 * @updateBy:
 * @copyright: 众阳健康
 */
@Data
public class ItemNode {

    /**
     * 文档id
     */
    private Long documentId;

    /**
     * 词频
     */
    private short count;

    /**
     * 出现位置
     */
    private List<Short> position;

    public ItemNode(Long documentId, short count, List<Short> position) {
        this.documentId = documentId;
        this.count = count;
        this.position = position;
    }
    public ItemNode(){

    }
}