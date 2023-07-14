package kz.smarthealth.scheduleservice.util;

import java.time.ZoneId;
import java.time.ZoneOffset;

public class AppConstants {

    private AppConstants() {
    }

    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final ZoneId UTC_ZONE_ID = ZoneId.of(ZoneOffset.UTC.toString());
}
