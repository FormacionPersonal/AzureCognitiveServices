/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.cursoazure.t3.voiceservices.azurespeehrecognition;

import java.util.concurrent.Future;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.textanalytics.models.*;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.TextAnalyticsClient;

import com.google.gson.*;
import com.squareup.okhttp.*;

import java.io.*;
import java.net.*;
import java.util.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

/**
 * Quickstart: recognize speech using the Speech SDK for Java.
 */
public class AzureSpeechRecognition {

    /**
     * @param args Arguments are ignored in this sample.
     */
    public static void main(String[] args) {

        String textoProcesado;

        // Speech service configuration
        String speechSubscriptionKey = "b766d7ea902143a2a8e8b57cc170721b";
        String speechServiceRegion = "southcentralus";

        // Analytics service configuration
        String analyticsSubscriptionKey = "36324462d3134a08a15837ea3f57b7f7";
        String analyticsEndPoint = "https://t4cursocefirevmmanalytics.cognitiveservices.azure.com/";

        // Change default console output to UTF-8
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
        }

        try {

            SpeechConfig config = SpeechConfig.fromSubscription(speechSubscriptionKey, speechServiceRegion);
            TextAnalyticsClient client = new TextAnalyticsClientBuilder()
                    .credential(new AzureKeyCredential(analyticsSubscriptionKey))
                    .endpoint(analyticsEndPoint)
                    .buildClient();

            assert (config != null);
            assert (client != null);
            // Configuramos algunos par??metros del idioma
            config.setSpeechRecognitionLanguage("es-ES");
            // Captura de voz desde el micr??fono
            textoProcesado = fromMic(config);

            // Captura continua de voz desde el micr??fono
            // fromMicContinuous(config);

            // Mostramos informaci??n sobre el texto 
            textAnalysis(client, textoProcesado);

            // Traduce el texto procesado
            textTranslation(textoProcesado);

            // Salida por los altavoces
            toSpeaker(config, textoProcesado);
            toSpeaker(config, "La ejecuci??n del programa ha terminado.");

        } catch (Exception ex) {
            System.out.println("Unexpected exception: " + ex.getMessage());

            assert (false);
            System.exit(1);
        }

    }

    public static String fromMic(SpeechConfig speechConfig) throws InterruptedException, ExecutionException {
        // Captura de audio desde el micro est??ndar

        String textoReconocido = "no se ha reconodio texto o ha surgido alg??n problema";

        AudioConfig audioConfig = AudioConfig.fromDefaultMicrophoneInput();
        SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig);

        System.out.println("Habla usando tu micr??fono (entrada de audio est??ndar).");
        Future<SpeechRecognitionResult> task = recognizer.recognizeOnceAsync();

        // Esperamos al resultado del reconocimiento
        SpeechRecognitionResult result = task.get();
        switch (result.getReason()) {
            case RecognizedSpeech:
                System.out.println("Texto reconocido: " + result.getText());
                textoReconocido = result.getText();
                break;
            case NoMatch:
                System.out.println("NOMATCH: No se puede reconocer texto en el audio capturado.");
                break;
            case Canceled:
                CancellationDetails cancellation = CancellationDetails.fromResult(result);
                System.out.println("CANCELED: Reason=" + cancellation.getReason());

                if (cancellation.getReason() == CancellationReason.Error) {
                    System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                    System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                    System.out.println("CANCELED: Has configurado correctamente los datos de la subscripci??n al servicio?");
                }
            default:
                System.out.println("Undefined error: Reason=" + result.getReason().toString());
                break;
        }
        return textoReconocido;
    }

    public static void textAnalysis(TextAnalyticsClient client, String texto) {

/*        texto = "La cuarta ola es ya una realidad en Navarra. "
                + "Es la autonom??a donde la pandemia de coronavirus ha comenzado a desbocarse de nuevo. "
                + "La incidencia del covid en Navarra supera los 436 casos por cada 100.000 habitantes en las dos ??ltimas semanas. "
                + "En la otra cara de la moneda, la Comunidad Valenciana contin??a con apenas una incidencia de 35 contagios nuevos. "
                + "Es la autonom??a con menos contagios por habitante del pa??s. "
                + "De hecho, Navarra multiplica por 12 la incidencia del virus de la Comunidad Valenciana. "
                + "Al igual que Navarra, Pa??s Vasco registra una incidencia alt??sima. "
                + "Con 358 contagios por cada 100.000 habitantes, supone diez veces m??s que los registrados en la Comunidad Valenciana en las dos ??ltimas semanas. "
                + "Madrid sigue siendo uno de los territorios con mayor incidencia del virus, pese a la densidad de poblaci??n que deber??a amortiguar el n??mero contagios por habitante.";
*/
        DocumentSentiment documentSentiment = client.analyzeSentiment(texto, "es-ES");
        System.out.println("Opini??n general sobre el texto: " + documentSentiment.getSentiment());
        System.out.println("  >> Positivo: " + documentSentiment.getConfidenceScores().getPositive());
        System.out.println("  >> Neutral: " + documentSentiment.getConfidenceScores().getNeutral());
        System.out.println("  >> Negativo: " + documentSentiment.getConfidenceScores().getNegative());
        System.out.println("");

        System.out.println("Frases y palabras clave del texto:");
        for (String keyPhrase : client.extractKeyPhrases(texto)) {
            System.out.println("  >> " + keyPhrase);
        }
        System.out.println("");

        System.out.println("Entidades detectadas en el texto: ");
        for (CategorizedEntity entity : client.recognizeEntities(texto)) {
            System.out.print("  >> Entidad reconocida: " + entity.getText());
            System.out.print("  >> Categor??a: " + entity.getCategory());
            System.out.print((entity.getSubcategory() == null) ? "" : "  >> Subcategor??a: " + entity.getSubcategory());
            System.out.print("  >> Confianza: " + entity.getConfidenceScore());
            System.out.println("");
        }
    }

    public static void toSpeaker(SpeechConfig speechConfig, String texto) throws InterruptedException, ExecutionException {

        // Captura de audio desde el micro est??ndar
        AudioConfig audioConfig = AudioConfig.fromDefaultSpeakerOutput();
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, audioConfig);

        synthesizer.SpeakSsml(getVoiceConfig(texto));
        //synthesizer.SpeakText(texto);
    }

    private static String getVoiceConfig(String texto) {
        String voiceSSML = "<speak version=\"1.0\" xmlns=\"https://www.w3.org/2001/10/synthesis\" xml:lang=\"en-US\">\n";
        voiceSSML += "<voice name=\"es-ES-Laura\">\n";
        voiceSSML += texto + "\n";
        voiceSSML += "</voice>\n";
        voiceSSML += "</speak>\n";

        return voiceSSML;
    }

    public static void textTranslation(String texto) throws IOException {
        // Translator service configuration
        String translatorSubscriptionKey = "2918972c1a3d453ba12c2a59491487e8";
        String translatorEndPoint = "https://api.cognitive.microsofttranslator.com/";

        // Preparar la URL
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.cognitive.microsofttranslator.com")
                .addPathSegment("/translate")
                .addQueryParameter("api-version", "3.0")
                .addQueryParameter("from", "es")
                .addQueryParameter("to", "en")
                .addQueryParameter("to", "de")
                .build();

        // Instanciar el cliente HTTP para realizar las peiticiones.
        OkHttpClient client = new OkHttpClient();

        // Realizar la petici??n POST.    
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "[{\"Text\": \"" + texto + "\"}]");
        Request request = new Request.Builder().url(url).post(body)
                .addHeader("Ocp-Apim-Subscription-Key", translatorSubscriptionKey)
                .addHeader("Ocp-Apim-Subscription-Region", "global")
                .addHeader("Content-type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
                
        // Procesar respuesta JSON
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(response.body().string());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("");
        System.out.println("Traducci??n de la respuesta recibida: ");
        System.out.println(gson.toJson(json));
        
        for(JsonElement element: json.getAsJsonArray().get(0).getAsJsonObject().get("translations").getAsJsonArray())
        {
            JsonObject translation = element.getAsJsonObject();
            System.out.println("  >> " + translation.get("to") + " --> " + translation.get("text"));
        }
        System.out.println("");
        
    }

    public static void fromMicContinuous(SpeechConfig speechConfig) throws InterruptedException, ExecutionException {
        Semaphore stopTranslationWithFileSemaphore;

        // Captura de audio desde el micro est??ndar
        AudioConfig audioConfig = AudioConfig.fromDefaultMicrophoneInput();
        SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig);

        // ***************************************
        // --- C??digo para captura continua de voz
        // First initialize the semaphore.
        stopTranslationWithFileSemaphore = new Semaphore(0);

        recognizer.recognizing.addEventListener(
                (s, e) -> {
                    System.out.println("RECOGNIZING: Text=" + e.getResult().getText());
                }
        );

        recognizer.recognized.addEventListener(
                (s, e) -> {
                    if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                        System.out.println("RECOGNIZED: Text=" + e.getResult().getText());
                    } else if (e.getResult().getReason() == ResultReason.NoMatch) {
                        System.out.println("NOMATCH: Speech could not be recognized.");
                    }
                }
        );

        recognizer.canceled.addEventListener(
                (s, e) -> {
                    System.out.println("CANCELED: Reason=" + e.getReason());

                    if (e.getReason() == CancellationReason.Error) {
                        System.out.println("CANCELED: ErrorCode=" + e.getErrorCode());
                        System.out.println("CANCELED: ErrorDetails=" + e.getErrorDetails());
                        System.out.println("CANCELED: Did you update the subscription info?");
                    }

                    stopTranslationWithFileSemaphore.release();
                }
        );

        recognizer.sessionStopped.addEventListener(
                (s, e) -> {
                    System.out.println("\n    Session stopped event.");
                    stopTranslationWithFileSemaphore.release();
                }
        );

        // Starts continuous recognition. Uses StopContinuousRecognitionAsync() to stop recognition.
        recognizer.startContinuousRecognitionAsync().get();

        // Waits for completion.
        stopTranslationWithFileSemaphore.acquire();

        // Stops recognition.
        recognizer.stopContinuousRecognitionAsync().get();

        System.out.println("Habla usando tu micr??fono (entrada de audio est??ndar).");
    }
}
