package lucns.gupy.rh.api;

import lucns.gupy.rh.GupyUtils;
import lucns.gupy.rh.models.Locality;
import lucns.gupy.utils.Annotator;

public class LocalityProvider extends BaseProvider {

    public LocalityProvider(ResponseCallback responseCallback) {
        super(responseCallback);
    }

    public void requestStates() {
        if (isRunning) return;
        isRunning = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                request = requestGet("https://servicodados.ibge.gov.br/api/v1/localidades/estados");
                if (responseCode == 200) {
                    Locality[] localities = GupyUtils.jsonToStates(request);
                    if (localities != null) new Annotator("States.json").setContent(request);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            isRunning = false;
                            responseCallback.onLocalityAvailable(localities);
                        }
                    });
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        isRunning = false;
                        responseCallback.onError(request, responseCode);
                    }
                });
            }
        });
        thread.start();
    }

    public void requestCities(int ufId) {
        if (isRunning) return;
        isRunning = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                request = requestGet("https://servicodados.ibge.gov.br/api/v1/localidades/estados/" + ufId + "/municipios");
                if (responseCode == 200) {
                    Locality[] localities = GupyUtils.jsonToCities(request);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            isRunning = false;
                            responseCallback.onLocalityAvailable(localities);
                        }
                    });
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        isRunning = false;
                        responseCallback.onError(request, responseCode);
                    }
                });
            }
        });
        thread.start();
    }
}
