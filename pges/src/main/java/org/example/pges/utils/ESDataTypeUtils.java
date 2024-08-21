package org.example.pges.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
     * 合并两个数组(包含去重逻辑)
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
     * @return {@link Long[] }
     */

    public static Long[] arrayListToArray(List<Long> list){
        if(CollUtil.isEmpty(list)){
            return new Long[0];
        }
        return list.toArray(new Long[0]);
    }

    /**
     * 将数组分段，每一段的最大长度为maxSize
     * @param array
     * @param maxSize
     * @return {@link List }<{@link Long[] }>
     */

    public static List<Long[]> splitArray(Long[] array,int maxSize){
        List<Long[]> result = new ArrayList<>();
        if(array.length < maxSize){
            result.add(array);
            return result;
        }
        int index = 0;
        while(index < array.length){
            if(index + maxSize > array.length){
                maxSize = array.length - index;
            }
            Long[] item = Arrays.copyOfRange(array, index, index + maxSize);
            index += maxSize;
            result.add(item);
        }
        return result;
    }
}