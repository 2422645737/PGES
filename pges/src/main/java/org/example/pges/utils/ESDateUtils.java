package org.example.pges.utils;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import org.example.pges.constance.ESDateConst;
import org.example.pges.constance.IndexNumConst;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ESDateUtils {
    private static List<Date[]> dateSegment = new ArrayList<>();

    static {
        Date startTime = ESDateConst.startTime;
        while(!startTime.after(new Date())){
            Date[] dates = new Date[2];
            dates[0] = startTime;
            dates[1] = DateUtil.offsetDay(startTime,IndexNumConst.DAY_INTERVAL);
            dateSegment.add(dates);
            startTime = DateUtil.offsetDay(startTime,IndexNumConst.DAY_INTERVAL);
        }
    }
    /**
     * 生成时间片段
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<Date[]> getDateSegment(Date startDate, Date endDate) {
        return getDateSegment(startDate,endDate, IndexNumConst.DAY_INTERVAL);
    };
    /**
     * 生成时间片段
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<Date[]> getDateSegment(Date startDate, Date endDate, int days) {
        List<Date[]> segments = new ArrayList<Date[]>();
        while(!startDate.after(endDate)){
            Date[] dates = new Date[2];
            dates[0] = startDate;
            dates[1] = DateUtil.offsetDay(startDate,days);
            segments.add(dates);
            startDate = DateUtil.offsetDay(startDate,days);
        }
        return segments;
    };

    public static boolean between(Date startDate,Date endDate,Date date){
        return !(date.before(startDate) || date.after(endDate));
    }

    /**
     * 获取当前时间对应的时间段
     * @param date
     * @return {@link Date[] }
     */

    public static Date[] getDateSegment(Date date){
        int left = 0;
        int right = dateSegment.size() - 1;
        while(left <= right){
            int mid = left + (right - left ) / 2;
            Date[] dates = dateSegment.get(mid);
            if(between(dates[0],dates[1],date)){
                return dates;
            }else if(!dates[0].before(date)){
                right = mid - 1;
            }else{
                left = mid + 1;
            }
        }
        return null;
    }
    /**
     * 将某个时间段分割成count个时间段
     * @param beginTime
     * @param endTime
     * @param count
     * @return {@link Date[] }
     */

    public static List<Date[]> splitDate(Date beginTime,Date endTime,int count){
        Long between = DateUtil.betweenDay(beginTime,endTime,true);
        //不能将10天拆成12份，上限就是两个日期之间的时间间隔
        if(between.intValue() < count){
            count = between.intValue();
        }
        int unit = between.intValue() / count;
        List<Date[]> result = new ArrayList<>(4);
        while(beginTime.before(endTime)){
            Date[] item = new Date[2];
            item[0] = beginTime;
            item[1] = DateUtil.offsetDay(beginTime,unit);
            beginTime = DateUtil.offsetDay(beginTime,unit);
            result.add(item);
        }
        return result;
    }

}