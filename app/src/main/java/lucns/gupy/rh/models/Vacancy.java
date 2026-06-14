package lucns.gupy.rh.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Vacancy {

    public static final String TYPE_ON_SITE = "on-site";
    public static final String TYPE_HYBRID = "hybrid";
    public static final String TYPE_REMOTE = "remote";


    public int id;
    public String name, publicationDate, expirationDate, workplaceType, type, enterpriseName;
    public String description, responsibilitiesDuties, requirementsQualifications, additionalInformation, address;
    public String url, urlLogo;
    public Locality locality;

    public void formatDates() {
        if (publicationDate != null) {
            if (publicationDate.equals("null")) publicationDate = null;
            publicationDate = formatDate(publicationDate);
        }

        if (expirationDate != null) {
            if (expirationDate.equals("null")) expirationDate = null;
            expirationDate = formatDate(expirationDate);
        }
    }

    public boolean isExpired() {
        if (expirationDate == null) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date currentDate = new Date();
        try {
            Date expiryDate = sdf.parse(expirationDate);
            if (expiryDate == null) return false;
            return expiryDate.before(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String formatDate(String dateTime) {
        if (dateTime == null) return null;
        if (dateTime.contains("Z")) {
            String[] segments = dateTime.split("T");
            String[] date = segments[0].split("-");
            return date[2] + "/" + date[1] + "/" + date[0];
        }
        if (dateTime.contains("-")) {
            String[] date = dateTime.split("-");
            return date[2] + "/" + date[1] + "/" + date[0];
        }
        return dateTime;
    }

    public long getPassedDays(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date startDate = sdf.parse(date);
            Date endDate = new Date();
            long diffInMillies = endDate.getTime() - startDate.getTime();
            return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
