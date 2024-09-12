package org.example.pges.utils;

import cn.hutool.core.collection.CollUtil;
import org.example.pges.entity.po.Document;
import org.example.pges.entity.po.ItemNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @description:
 * @fileName: IndexBuilderUtil
 * @author: wanghui
 * @createAt: 2024/09/12 04:18:33
 * @updateBy:
 * @copyright: 众阳健康
 */

public class IndexBuilder {
    public static List<ItemNode> buildIndexNodeList(String key, Collection<Document> documents) {
        if(CollUtil.isEmpty(documents)){
            return new ArrayList<>();
        }
        List<ItemNode> result = new ArrayList<>();
        for (Document document : documents) {
            String contentText = document.getContentText();
            List<Short> positions = new ArrayList<>();
            short pos = (short) contentText.indexOf(key);
            while(pos != -1){
                positions.add(pos);
                pos = (short) contentText.indexOf(key,pos + key.length());
            }
            result.add(new ItemNode(document.getOutEmrDetailId(), (short) positions.size(),positions));
        }
        return result;
    }
}