package lucns.gupy.rh.api;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import lucns.gupy.rh.models.Vacancy;
import lucns.gupy.utils.Annotator;

public class GupyPageRetriever {

    private Vacancy vacancy;

    public GupyPageRetriever(Vacancy vacancy) {
        this.vacancy = vacancy;
    }

    public Vacancy vacancyFromHtml(String code) {
        code = autoIndentCode(code);
        //new Annotator(vacancy.name + ".html").setContent(code);
        String[] lines = code.split("\n");
        String json = null;
        for (String line : lines) {
            if (line.contains("__NEXT_DATA__")) {
                json = line.substring(line.lastIndexOf(">") + 1);
                break;
            }
        }
        if (json == null) return null;
        try {
            JSONObject jsonObject = new JSONObject(json).getJSONObject("props").getJSONObject("pageProps").getJSONObject("job");
            //new Annotator("Data.json").setContent(jsonObject.toString(4));
            //Log.d("Lucas", "JSON Ok");
            vacancy.description = jsonObject.optString("description", null);
            vacancy.responsibilitiesDuties = jsonObject.optString("responsibilities", null);
            vacancy.requirementsQualifications = jsonObject.optString("prerequisites", null);
            vacancy.additionalInformation = jsonObject.optString("relevantExperiences", null);
            vacancy.address = jsonObject.optString("addressLine", null);
            removeHtml();
            return vacancy;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void removeHtml() {
        StringBuilder builder = new StringBuilder();
        if (vacancy.description != null) {
            char[] chars = vacancy.description.toCharArray();
            boolean inside = false;
            for (char c : chars) {
                if (c == '<') {
                    inside = true;
                    continue;
                } else if (c == '>') {
                    inside = false;
                    continue;
                }
                if (inside) continue;
                builder.append(c);
            }
            vacancy.description = builder.toString();
            vacancy.description = vacancy.description.replaceAll(";", ";\n");
            vacancy.description = vacancy.description.replaceAll("&nbsp", " ");
        }
        builder = new StringBuilder();
        if (vacancy.responsibilitiesDuties != null) {
            char[] chars = vacancy.responsibilitiesDuties.toCharArray();
            boolean inside = false;
            for (char c : chars) {
                if (c == '<') {
                    inside = true;
                    continue;
                } else if (c == '>') {
                    inside = false;
                    continue;
                }
                if (inside) continue;
                builder.append(c);
            }
            vacancy.responsibilitiesDuties = builder.toString();
            vacancy.responsibilitiesDuties = vacancy.responsibilitiesDuties.replaceAll(";", ";\n");
            vacancy.responsibilitiesDuties = vacancy.responsibilitiesDuties.replaceAll("&nbsp", " ");
        }
        builder = new StringBuilder();
        if (vacancy.requirementsQualifications != null) {
            char[] chars = vacancy.requirementsQualifications.toCharArray();
            boolean inside = false;
            for (char c : chars) {
                if (c == '<') {
                    inside = true;
                    continue;
                } else if (c == '>') {
                    inside = false;
                    continue;
                }
                if (inside) continue;
                builder.append(c);
            }
            vacancy.requirementsQualifications = builder.toString();
            vacancy.requirementsQualifications = vacancy.requirementsQualifications.replaceAll(";", ";\n");
            vacancy.requirementsQualifications = vacancy.requirementsQualifications.replaceAll("&nbsp", " ");
        }
        builder = new StringBuilder();
        if (vacancy.additionalInformation != null) {
            char[] chars = vacancy.additionalInformation.toCharArray();
            boolean inside = false;
            for (char c : chars) {
                if (c == '<') {
                    inside = true;
                    continue;
                } else if (c == '>') {
                    inside = false;
                    continue;
                }
                if (inside) continue;
                builder.append(c);
            }
            vacancy.additionalInformation = builder.toString();
            vacancy.additionalInformation = vacancy.additionalInformation.replaceAll(";", ";\n");
            vacancy.additionalInformation = vacancy.additionalInformation.replaceAll("&nbsp", " ");
        }
    }

    private String autoIndentCode(String code) {
        StringBuilder builder = new StringBuilder();
        char[] characters = code.toCharArray();
        int i = 0;
        while (i < characters.length) {
            if (builder.length() > 0) {
                if (characters[i] == '<') builder.append('\n');
            }
            builder.append(characters[i]);
            i++;
        }
        return builder.toString();
    }
}
