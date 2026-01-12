package lucns.gupy.rh.models;

import android.util.Log;

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
}
