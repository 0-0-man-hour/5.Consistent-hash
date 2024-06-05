package com.zeromh.consistenthash.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
    public static String getNowDate() {
        return formatter.format(new Date());
    }
}
