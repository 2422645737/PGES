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
        return date.after(startDate) && date.before(endDate);
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
}