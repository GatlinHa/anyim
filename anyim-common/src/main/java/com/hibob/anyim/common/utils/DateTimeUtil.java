package com.hibob.anyim.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
public class DateTimeUtil {

    private final static List<String> FORMAT_LIST = Arrays.asList(
        "yyyy-MM-dd HH:mm:ss.SSSSSS",
        "yyyy-MM-dd HH:mm:ss"
    );

    public static Date getDateFromStr(String timeStr, String format) {
        formatValidate(format);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(timeStr);
        } catch (ParseException e) {
            log.error("getDateFromStr error, timeStr: {}, format: {}", timeStr, format);
            throw new RuntimeException(e);
        }
    }

    public static long getMillisecondFromStr(String timeStr, String format) {
        formatValidate(format);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(timeStr).getTime();
        } catch (ParseException e) {
            log.error("getDateFromStr error, timeStr: {}, format: {}", timeStr, format);
            throw new RuntimeException(e);
        }
    }

    private static void formatValidate(String format) {
        if (!FORMAT_LIST.contains(format)) {
            throw new RuntimeException(String.format("This format: %s is not supported", format));
        }
    }
}
