package org.example.pges.utils;

import cn.hutool.core.collection.CollUtil;

import java.util.Arrays;
import java.util.List;

/**
 * @description: ES数据类型工具类
 * @fileName: ESDataTypeUtils
 * @author: wanghui
 * @createAt: 2024/08/01 01:53:47
 * @updateBy:
 */

public class ESDataTypeUtils {

    /**
     * 合并两个数组
     * @param arr1
     * @param arr2
     * @return {@link long[] }
     */

    public static Long[] mergeArray(Long[] arr1,Long[] arr2){
        if(arr1 == null){
            return arr2;
        }
        if(arr2 == null){
            return arr1;
        }
        Long[] newArray = new Long[arr1.length + arr2.length];
        System.arraycopy(arr1,0,newArray,0,arr1.length);
        System.arraycopy(arr2,0,newArray,arr1.length,arr2.length);
        return newArray;
    }
    /**
     * 合并两个数组
     * @param arr1
     * @param arr2
     * @return {@link long[] }
     */

    public static Long[] mergeArrayDistinct(Long[] arr1,Long[] arr2){
        Long[] longs = mergeArray(arr1, arr2);
        if(longs == null){
            return new Long[0];
        }
        //去重
        return  Arrays.asList(longs).stream().distinct().toList().toArray(new Long[0]);
    }
    /**
     * 将List<Long>转化为Long[]
     * @param list
     * @return {@link long[] }
     */

    public static Long[] arrayListToArray(List<Long> list){
        if(CollUtil.isEmpty(list)){
            return new Long[0];
        }
        return list.toArray(new Long[0]);
    }


}