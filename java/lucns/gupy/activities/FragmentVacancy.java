package lucns.gupy.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

import lucns.gupy.R;
import lucns.gupy.rh.api.ResponseCallback;
import lucns.gupy.rh.api.VacancyRequester;
import lucns.gupy.rh.models.Vacancy;
import lucns.gupy.utils.Notify;
import lucns.gupy.views.SmoothRelativeLayout;
import lucns.gupy.views.slider.FragmentView;

public class FragmentVacancy extends FragmentView {

    private Vacancy vacancy;
    private VacancyRequester vacancyRequester;
    private TextView textVacancyDescription, textResponsibilitiesDuties, textRequirementsQualifications, textAdditionalInformation, textLocation;
    private boolean loaded;

    public FragmentVacancy(Activity activity, Vacancy vacancy) {
        super(activity);
        this.vacancy = vacancy;
    }

    @Override
    public void onCreate() {
        setContentView(R.layout.fragment_vacancy);
        SmoothRelativeLayout rootTexts = findViewById(R.id.rootTexts);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView textStatus = findViewById(R.id.textStatus);
        TextView textTitle = findViewById(R.id.textTitle);
        TextView textExpiration = findViewById(R.id.textExpirationDateValue);
        TextView textPublished = findViewById(R.id.textPublishedDateValue);
        vacancy.formatDates();
        if (vacancy.publicationDate != null) textPublished.setText(vacancy.publicationDate);
        if (vacancy.expirationDate != null) textExpiration.setText(vacancy.expirationDate);
        textVacancyDescription = findViewById(R.id.textVacancyDescriptionValue);
        textResponsibilitiesDuties = findViewById(R.id.textResponsibilitiesDutiesValue);
        textRequirementsQualifications = findViewById(R.id.textRequirementsQualificationsValue);
        textAdditionalInformation = findViewById(R.id.textAdditionalInformationValue);
        textLocation = findViewById(R.id.textLocation);
        TextView textType = findViewById(R.id.textType);
        TextView textEnterpriseName = findViewById(R.id.textEnterpriseName);
        textEnterpriseName.setText(vacancy.enterpriseName);
        textTitle.setText(vacancy.name);
        String locality = "";
        if (vacancy.locality.city != null) locality = vacancy.locality.city;
        if (vacancy.locality.state != null) {
            if (!locality.isEmpty()) locality += ", ";
            locality += vacancy.locality.state;
        }
        if (!vacancy.locality.country.isEmpty()) {
            if (!locality.isEmpty()) locality += " - ";
            locality += vacancy.locality.country;
        }
        textLocation.setText(locality);
        String type = "";
        if (vacancy.workplaceType.contains(Vacancy.TYPE_ON_SITE) && vacancy.workplaceType.contains(Vacancy.TYPE_HYBRID) && vacancy.workplaceType.contains(Vacancy.TYPE_REMOTE)) {
            type += getString(R.string.in_person);
            type += ", ";
            type += getString(R.string.hybrid);
            type += " " + getString(R.string.and) + " ";
            type += getString(R.string.remote);
        } else if (vacancy.workplaceType.contains(Vacancy.TYPE_ON_SITE) && vacancy.workplaceType.contains(Vacancy.TYPE_HYBRID)) {
            type += getString(R.string.in_person);
            type += " " + getString(R.string.and) + " ";
            type += getString(R.string.hybrid);
        } else if (vacancy.workplaceType.contains(Vacancy.TYPE_HYBRID) && vacancy.workplaceType.contains(Vacancy.TYPE_REMOTE)) {
            type += getString(R.string.hybrid);
            type += " " + getString(R.string.and) + " ";
            type += getString(R.string.remote);
        } else if (vacancy.workplaceType.contains(Vacancy.TYPE_ON_SITE) && vacancy.workplaceType.contains(Vacancy.TYPE_REMOTE)) {
            type += getString(R.string.in_person);
            type += " " + getString(R.string.and) + " ";
            type += getString(R.string.remote);
        } else if (vacancy.workplaceType.contains(Vacancy.TYPE_ON_SITE)) {
            type = getString(R.string.in_person);
        } else if (vacancy.workplaceType.contains(Vacancy.TYPE_HYBRID)) {
            type = getString(R.string.hybrid);
        } else if (vacancy.workplaceType.contains(Vacancy.TYPE_REMOTE)) {
            type = getString(R.string.remote);
        }
        textType.setText(type);

        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.buttonRegister) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(vacancy.url));
                    startActivity(browserIntent);
                } else {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", vacancy.url);
                    clipboard.setPrimaryClip(clip);
                    Notify.showToast(R.string.copied);
                }
            }
        };
        Button buttonRegister = findViewById(R.id.buttonRegister);
        Button buttonCopy = findViewById(R.id.buttonCopy);
        buttonRegister.setOnClickListener(onClick);
        buttonCopy.setOnClickListener(onClick);

        vacancyRequester = new VacancyRequester(new ResponseCallback() {

            @Override
            public void onVacancyRetrieved(Vacancy vacancy) {
                progressBar.setVisibility(View.INVISIBLE);
                textStatus.setVisibility(View.INVISIBLE);
                rootTexts.setVisibility(View.VISIBLE);
                if (vacancy.description != null) textVacancyDescription.setText(vacancy.description);
                if (vacancy.responsibilitiesDuties != null) textResponsibilitiesDuties.setText(vacancy.responsibilitiesDuties);
                if (vacancy.requirementsQualifications != null) textRequirementsQualifications.setText(vacancy.requirementsQualifications);
                if (vacancy.additionalInformation != null) textAdditionalInformation.setText(vacancy.additionalInformation);
                if (vacancy.address != null) {
                    CharSequence c = textLocation.getText();
                    if (c != null && c.length() > 0) {
                        textLocation.setText(c.toString() + "\n" + vacancy.address);
                    }
                }
            }

            @Override
            public void onError(String message, int code) {
                progressBar.setVisibility(View.INVISIBLE);
                textStatus.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                loaded = true;
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onResume() {
        if (loaded) return;
        if (!vacancyRequester.isRunning()) vacancyRequester.requester(vacancy);
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {

    }
}
