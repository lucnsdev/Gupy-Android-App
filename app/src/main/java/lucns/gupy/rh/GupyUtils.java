package lucns.gupy.rh;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Comparator;

import lucns.gupy.rh.models.Enterprise;
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
            Arrays.sort(vacancies, new Comparator<Vacancy>() {

                @Override
                public int compare(Vacancy v, Vacancy v2) {
                    if (v == null || v2 == null) return 0;
                    return Integer.compare(v2.id, v.id);
                }
            });
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
            Arrays.sort(vacancies, new Comparator<Vacancy>() {

                @Override
                public int compare(Vacancy v, Vacancy v2) {
                    if (v == null || v2 == null) return 0;
                    return Integer.compare(v2.id, v.id);
                }
            });
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

    public static Enterprise[] jsonToEnterprises(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            Enterprise[] enterprises = new Enterprise[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonEnterprise = jsonArray.getJSONObject(i);
                String url = jsonEnterprise.getString("careerPageUrl");
                enterprises[i] = new Enterprise(jsonEnterprise.getInt("careerPageId"), jsonEnterprise.getString("careerPageName"), url.substring(0, url.lastIndexOf(".io") + 3));
            }
            return enterprises;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Enterprise[] getAllEnterprisesWithVacancies(String name) {
        Annotator annotator = new Annotator(name);
        if (!annotator.exists()) return null;
        try {
            JSONObject jsonObject = new JSONObject(annotator.getContent());
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            Enterprise[] enterprises = new Enterprise[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonEnterprise = jsonArray.getJSONObject(i);
                JSONArray jsonVacancies = jsonEnterprise.getJSONArray("vacancies");
                Enterprise enterprise = new Enterprise(jsonEnterprise.getInt("id"), jsonEnterprise.getString("name"), jsonEnterprise.getString("url"));
                Vacancy[] vacancies = new Vacancy[jsonVacancies.length()];
                for (int a = 0; a < jsonVacancies.length(); a++) {
                    JSONObject jsonVacancy = jsonVacancies.getJSONObject(a);
                    Vacancy vacancy = new Vacancy();
                    vacancy.id = jsonVacancy.getInt("id");
                    vacancy.name = jsonVacancy.getString("name");
                    vacancy.url = enterprise.url + "/jobs/" + vacancy.id;
                    vacancy.locality = new Locality(jsonVacancy.getString("country"), jsonVacancy.getString("state"), jsonVacancy.getString("city"));
                    vacancies[a] = vacancy;
                }
                enterprise.vacancies = vacancies;
                enterprises[i] = enterprise;
            }
            return enterprises;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void putEnterpriseIntoGlobalData(Enterprise enterprise) {
        Annotator annotator = new Annotator("GlobalEnterprisesDataTemp.json");
        try {
            JSONObject jsonObject;
            if (annotator.exists()) {
                jsonObject = new JSONObject(annotator.getContent());
            } else {
                jsonObject = new JSONObject();
                jsonObject.put("data", new JSONArray());
            }
            jsonObject.put("timestamp", System.currentTimeMillis());
            JSONArray jsonVacancies = new JSONArray();
            if (enterprise.vacancies != null) {
                for (Vacancy vacancy : enterprise.vacancies) {
                    JSONObject jsonVacancy = new JSONObject();
                    jsonVacancy.put("id", vacancy.id);
                    jsonVacancy.put("name", vacancy.name);
                    jsonVacancy.put("country", vacancy.locality.country);
                    jsonVacancy.put("state", vacancy.locality.state);
                    jsonVacancy.put("city", vacancy.locality.city);
                    jsonVacancies.put(jsonVacancy);
                }
            }
            JSONObject jsonEnterprise = new JSONObject();
            jsonEnterprise.put("id", enterprise.id);
            jsonEnterprise.put("name", enterprise.name);
            jsonEnterprise.put("url", enterprise.url);
            jsonEnterprise.put("vacancies", jsonVacancies);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            jsonArray.put(jsonEnterprise);
            annotator.setContent(jsonObject.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Vacancy[] extractVacanciesFromJson(String json) {
        try {
            JSONArray jsonArray = new JSONObject(json).getJSONObject("props").getJSONObject("pageProps").getJSONArray("jobs");
            Vacancy[] vacancies = new Vacancy[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonVacancy = jsonArray.getJSONObject(i);
                JSONObject jsonWorkplace = jsonVacancy.getJSONObject("workplace");
                JSONObject jsonAddress = jsonWorkplace.getJSONObject("address");
                Vacancy vacancy = new Vacancy();
                vacancy.id = jsonVacancy.getInt("id");
                vacancy.name = jsonVacancy.getString("title");
                vacancy.type = jsonVacancy.getString("type"); // vacancy_type_internship, vacancy_type_effective,
                vacancy.workplaceType = jsonWorkplace.optString("workplaceType"); // on-site, hybrid, remote
                vacancy.locality = new Locality(jsonAddress.getString("country"), jsonAddress.getString("state"), jsonAddress.getString("city"));
                vacancies[i] = vacancy;
            }
            return vacancies;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setAllEnterprises(Enterprise[] enterprises) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Enterprise e : enterprises) {
                JSONObject jsonEnterprise = new JSONObject();
                jsonEnterprise.put("id", e.id);
                jsonEnterprise.put("name", e.name);
                jsonEnterprise.put("url", e.url);
                jsonArray.put(jsonEnterprise);
            }
            new Annotator("Enterprises.json").setContent(jsonArray.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
