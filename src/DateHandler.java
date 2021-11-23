import java.util.Calendar;
import java.util.TimeZone;

public class DateHandler {


    public int getHour(){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    public int getMinute(){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        int minute = cal.get(Calendar.MINUTE);
        return minute;
    }

}
