package org.example.pges.utils;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import org.example.pges.constance.IndexNumConst;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ESDateUtils {
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
        while(startDate.before(endDate)){
            Date[] dates = new Date[2];
            dates[0] = startDate;
            dates[1] = DateUtil.offsetDay(startDate,days);
            segments.add(dates);
            startDate = DateUtil.offsetDay(startDate,days + 1);
        }
        return segments;
    };

    public static boolean between(Date startDate,Date endDate,Date date){
        return date.after(startDate) && date.before(endDate);
    }
}