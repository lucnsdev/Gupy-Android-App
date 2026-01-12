package lucns.gupy.rh;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Comparator;

import lucns.gupy.rh.models.Locality;
import lucns.gupy.rh.models.Vacancy;
import lucns.gupy.utils.Annotator;

public class GupyUtils {

    public static void setViewedVacancies(Vacancy[] vacancies) {
        try {
            JSONObject json = new JSONObject();
            json.put("data", new JSONArray());
            new Annotator("VacanciesNews.json").setContent(json.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Annotator annotator = new Annotator("VacanciesViewed.json");
        try {
            JSONObject jsonObject;
            if (annotator.exists()) {
                jsonObject = new JSONObject(annotator.getContent());
            } else {
                jsonObject = new JSONObject();
                jsonObject.put("data", new JSONArray());
            }
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (Vacancy vacancy : vacancies) {
                JSONObject jsonVacancy = new JSONObject();
                jsonVacancy.put("id", vacancy.id);
                jsonVacancy.put("name", vacancy.name);
                jsonVacancy.put("description", vacancy.description);
                jsonVacancy.put("careerPageName", vacancy.enterpriseName);
                jsonVacancy.put("careerPageLogo", vacancy.urlLogo);
                jsonVacancy.put("type", vacancy.type);
                jsonVacancy.put("workplaceType", vacancy.workplaceType);
                jsonVacancy.put("jobUrl", vacancy.url);
                jsonVacancy.put("country", vacancy.locality.country);
                jsonVacancy.put("state", vacancy.locality.state);
                jsonVacancy.put("city", vacancy.locality.city);
                jsonVacancy.put("publishedDate", vacancy.publicationDate);
                jsonVacancy.put("applicationDeadline", vacancy.expirationDate);
                jsonArray.put(jsonVacancy);
            }
            annotator.setContent(jsonObject.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Vacancy[] getViewedVacancies() {
        Annotator annotator = new Annotator("VacanciesViewed.json");
        if (!annotator.exists()) return null;
        Vacancy[] vacancies = null;
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            vacancies = new Vacancy[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonVacancy = jsonArray.getJSONObject(i);
                Vacancy vacancy = new Vacancy();
                vacancy.id = jsonVacancy.getInt("id");
                vacancy.name = jsonVacancy.getString("name");
                vacancy.description = jsonVacancy.getString("description");
                vacancy.enterpriseName = jsonVacancy.getString("careerPageName");
                vacancy.urlLogo = jsonVacancy.getString("careerPageLogo");
                vacancy.type = jsonVacancy.getString("type"); // vacancy_type_internship, vacancy_type_effective,
                vacancy.workplaceType = jsonVacancy.optString("workplaceType"); // on-site, hybrid, remote
                vacancy.url = jsonVacancy.getString("jobUrl");
                vacancy.locality = new Locality(jsonVacancy.getString("country"), jsonVacancy.getString("state"), jsonVacancy.getString("city"));
                vacancy.publicationDate = jsonVacancy.getString("publishedDate");
                vacancy.expirationDate = jsonVacancy.optString("applicationDeadline", null);
                vacancies[i] = vacancy;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return vacancies;
    }

    public static void setNewsVacancies(Vacancy[] vacancies) {
        Annotator annotator = new Annotator("VacanciesNews.json");
        try {
            JSONArray jsonArray = new JSONArray();
            for (Vacancy vacancy : vacancies) {
                JSONObject jsonVacancy = new JSONObject();
                jsonVacancy.put("id", vacancy.id);
                jsonVacancy.put("name", vacancy.name);
                jsonVacancy.put("description", vacancy.description);
                jsonVacancy.put("careerPageName", vacancy.enterpriseName);
                jsonVacancy.put("careerPageLogo", vacancy.urlLogo);
                jsonVacancy.put("type", vacancy.type);
                jsonVacancy.put("workplaceType", vacancy.workplaceType);
                jsonVacancy.put("jobUrl", vacancy.url);
                jsonVacancy.put("country", vacancy.locality.country);
                jsonVacancy.put("state", vacancy.locality.state);
                jsonVacancy.put("city", vacancy.locality.city);
                jsonVacancy.put("publishedDate", vacancy.publicationDate);
                jsonVacancy.put("applicationDeadline", vacancy.expirationDate);
                jsonArray.put(jsonVacancy);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", jsonArray);
            annotator.setContent(jsonObject.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Vacancy[] getNewVacancies() {
        Annotator annotator = new Annotator("VacanciesNews.json");
        if (!annotator.exists()) return null;
        Vacancy[] vacancies = null;
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            vacancies = new Vacancy[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonVacancy = jsonArray.getJSONObject(i);
                Vacancy vacancy = new Vacancy();
                vacancy.id = jsonVacancy.getInt("id");
                vacancy.name = jsonVacancy.getString("name");
                vacancy.description = jsonVacancy.getString("description");
                vacancy.enterpriseName = jsonVacancy.getString("careerPageName");
                vacancy.urlLogo = jsonVacancy.getString("careerPageLogo");
                vacancy.type = jsonVacancy.getString("type"); // vacancy_type_internship, vacancy_type_effective,
                vacancy.workplaceType = jsonVacancy.optString("workplaceType"); // on-site, hybrid, remote
                vacancy.url = jsonVacancy.getString("jobUrl");
                vacancy.locality = new Locality(jsonVacancy.getString("country"), jsonVacancy.getString("state"), jsonVacancy.getString("city"));
                vacancy.publicationDate = jsonVacancy.getString("publishedDate");
                vacancy.expirationDate = jsonVacancy.getString("applicationDeadline");
                vacancies[i] = vacancy;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return vacancies;
    }

    public static Vacancy[] getRegisteredVacancies() {
        Annotator annotator = new Annotator("VacanciesRegistered.json");
        if (!annotator.exists()) return null;
        Vacancy[] vacancies = null;
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            vacancies = new Vacancy[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonVacancy = jsonArray.getJSONObject(i);
                Vacancy vacancy = new Vacancy();
                vacancy.name = jsonVacancy.getString("name");
                vacancy.locality = new Locality(jsonVacancy.optString("country", null), jsonVacancy.optString("state", null), jsonVacancy.optString("city", null));
                vacancy.workplaceType = jsonVacancy.optString("workplaceType", null);
                vacancies[i] = vacancy;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return vacancies;
    }

    public static Vacancy[] jsonToVacancies(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            Vacancy[] vacancies = new Vacancy[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonVacancy = jsonArray.getJSONObject(i);
                Vacancy vacancy = new Vacancy();
                vacancy.id = jsonVacancy.getInt("id");
                vacancy.name = jsonVacancy.getString("name");
                vacancy.description = jsonVacancy.getString("description");
                vacancy.enterpriseName = jsonVacancy.getString("careerPageName");
                vacancy.urlLogo = jsonVacancy.getString("careerPageLogo");
                vacancy.type = jsonVacancy.getString("type"); // vacancy_type_internship, vacancy_type_effective,
                vacancy.workplaceType = jsonVacancy.optString("workplaceType"); // on-site, hybrid, remote
                vacancy.url = jsonVacancy.getString("jobUrl");
                vacancy.locality = new Locality(jsonVacancy.getString("country"), jsonVacancy.getString("state"), jsonVacancy.getString("city"));
                vacancy.publicationDate = jsonVacancy.getString("publishedDate");
                vacancy.expirationDate = jsonVacancy.getString("applicationDeadline");
                vacancies[i] = vacancy;
            }
            return vacancies;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Locality[] jsonToStates(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            Locality[] localities = new Locality[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                Locality locality = new Locality();
                locality.state = jsonData.getString("nome");
                locality.id = jsonData.getInt("id");
                localities[i] = locality;
            }
            Arrays.sort(localities, new Comparator<Locality>() {
                @Override
                public int compare(Locality a, Locality b) {
                    return a.state.compareTo(b.state);
                }
            });
            return localities;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Locality[] jsonToCities(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            Locality[] localities = new Locality[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                Locality locality = new Locality();
                locality.city = jsonData.getString("nome");
                locality.id = jsonData.getInt("id");
                localities[i] = locality;
            }
            return localities;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
