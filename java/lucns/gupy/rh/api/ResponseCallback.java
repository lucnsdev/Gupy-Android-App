package lucns.gupy.rh.api;

import lucns.gupy.rh.models.Locality;
import lucns.gupy.rh.models.Vacancy;

public abstract class ResponseCallback {

    public abstract void onError(String message, int code);

    public void onLocalityAvailable(Locality[] localities) {}

    public void onSuggestionsReceived(String[] suggestions) {
    }

    public void onVacanciesAvailable(Vacancy[] vacancies) {}

    public void onVacancyRetrieved(Vacancy vacancy) {}

    public void onFinish() {}
}
