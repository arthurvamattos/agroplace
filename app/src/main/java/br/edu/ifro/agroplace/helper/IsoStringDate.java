package br.edu.ifro.agroplace.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class IsoStringDate {

    public static String getIsoStringDate(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        return sdf.format(date);
    }
}
