package org.example.pges.constance;

import java.util.Calendar;
import java.util.Date;

/**
 * @description: 时间常量
 * @fileName: ESDateConst
 * @author: wanghui
 * @createAt: 2024/08/02 02:44:32
 * @updateBy:
 */

public class ESDateConst {

    public static Date startTime;
    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        startTime = calendar.getTime();
    }
}