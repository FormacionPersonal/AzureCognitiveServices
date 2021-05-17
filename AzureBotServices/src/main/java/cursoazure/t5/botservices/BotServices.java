/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cursoazure.t5.botservices;

import com.microsoft.azure.cognitiveservices.knowledge.qnamaker.*;
import com.microsoft.azure.cognitiveservices.knowledge.qnamaker.models.*;
//import com.microsoft.rest.RestClient;
//import com.microsoft.rest.credentials.ServiceClientCredentials;
//import com.microsoft.azure.cognitiveservices.language.luis.runtime.*;
//import com.microsoft.azure.cognitiveservices.language.luis.runtime.implementation.PredictionsImpl;
//import com.microsoft.azure.cognitiveservices.language.luis.runtime.models.*;

import com.google.gson.*;

import java.io.*;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.commons.logging.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VicenteMartínez
 */
public class BotServices {

    private static String QNA_ENDPOINT_KEY = "cb637ebd-670e-4154-815f-ceeae387c88b";
    private static String QNA_ENDPOINT = "https://qnamakercursocefirevmm.azurewebsites.net";
    private static String QNA_KBID = "eecc1efb-27df-49a1-920b-04f5de875959";

    private static String LUIS_PREDICTION_KEY = "36a35ca6b7d74284aa2c747740d0cfbe";
    private static String LUIS_ENDPOINT = "https://t5cursocefirevmmlanguageunderstanding.cognitiveservices.azure.com/";
    private static String LUIS_APPID = "d62d064c-432d-48cf-aef8-263922031e90";

    public static void main(String[] args) {
        String userQuestion;
        JsonElement rootElement;

        // Change default console output to UTF-8
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
        }
        try {
            while (true) {
                // Solicitamos la pregunta al usuario
                userQuestion = askUser();

                // Hacemos preguntas para detectar los "intents" en LUIS
                //query_LUIS_kb_withSDK(LUIS_APPID);
                boolean foundIntent = query_LUIS_kb_withREST(QNA_KBID, userQuestion);

                // Hacemos preguntas a la base de conocimientos si no se ha 
                // detectado ninguna intent con LUIS
                if (!foundIntent) {
                    query_QnA_kb(QNA_KBID, userQuestion);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(BotServices.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String askUser() {
        String userInput;
        Scanner reader = new Scanner(System.in);

        System.out.print("\033[32m Introduce tu consulta > ");
        userInput = reader.nextLine();

        return userInput;
    }

    /**
     * // Realizamos preguntas en bucle a la BBDD de conocimientos recibida
     * como parámetro public static void query_LUIS_kb_withSDK(String app_id)
     * throws IOException { String userQuestion; Scanner reader = new
     * Scanner(System.in);
     *
     * LuisRuntimeAPI client = LuisRuntimeManager.authenticate(LUIS_ENDPOINT,
     * LUIS_PREDICTION_KEY);
     *
     * while (true) { // Solicitamos la pregunta al usuario
     * System.out.print("Introduce tu pregunta: "); userQuestion =
     * reader.nextLine();
     *
     * // Realizamos la consulta a la Knowledge Base a través del SDK
     * LuisResult result = client.predictions().resolve()
     * .withAppId(UUID.fromString(app_id).toString()) .withQuery(userQuestion)
     * .withStaging(true) .execute();
     *
     * if (result.intents() != null) { System.out.println("Intents: "); for
     * (IntentModel intent : result.intents()) { System.out.println("\t" +
     * intent.intent() + ". Score: " + intent.score()); } } if
     * (result.entities() != null) { System.out.println("Entities: "); for
     * (EntityModel entity : result.entities()) { System.out.println("\t" +
     * entity.entity() + ". Type: " + entity.type()); } } } }
     */
    
    // Realizamos preguntas en bucle a la BBDD de conocimientos recibida como parámetro
    public static boolean query_LUIS_kb_withREST(String app_id, String userQuestion) throws IOException {
        JsonElement rootElement;
        boolean foundIntent = false;

        // Realizamos la consulta a la Knowledge Base a través del API REST            
        rootElement = queryREST(userQuestion);

        // Parseamos la respuesta para sacar la intención principal detectada
        String topIntent = rootElement.getAsJsonObject().getAsJsonObject("prediction").get("topIntent").getAsString();
        if (!topIntent.equals("None")) {
            System.out.println("\033[34m Intención detectada> " + topIntent);
            if (topIntent.equals("Salir"))
                System.exit(0);

            // Parseamos la respuesta para sacar ls posibles entidades detectadas
            for (Map.Entry<String, JsonElement> entity : rootElement.getAsJsonObject().getAsJsonObject("prediction").get("entities").getAsJsonObject().entrySet()) {
                if (!entity.getKey().startsWith("$")) {
                        System.out.println("\033[34m  >> Entidad asociada> " + entity.getKey() + ": " + entity.getValue().getAsString());
                }
            }
            foundIntent = true;
        } else {
            System.err.println("\033[31m Búsqueda de intención con LUIS: No se ha podido determinar la intención.");
            System.err.println("\033[31m Continuamos la búsqueda en la Knowledge Base de QnAMaker");
        }

        return foundIntent;
    }

    public static JsonElement queryREST(String userQuestion) {

        HttpClient httpclient = HttpClients.createDefault();

        try {
            // Preparar la URL y los parámetros necesarios
            URIBuilder endpointURLbuilder = new URIBuilder(LUIS_ENDPOINT + "luis/prediction/v3.0/apps/" + LUIS_APPID + "/slots/staging/predict?");
            endpointURLbuilder.setParameter("query", userQuestion);
            endpointURLbuilder.setParameter("subscription-key", LUIS_PREDICTION_KEY);
            endpointURLbuilder.setParameter("show-all-intents", "true");
            endpointURLbuilder.setParameter("verbose", "true");

            // Crear la petición HTTP a partir de la URL y hacer el request
            HttpGet request = new HttpGet(endpointURLbuilder.build());
            HttpResponse response = httpclient.execute(request);

            // Get the response.
            HttpEntity entity = response.getEntity();

            //Procesar respuesta JSON 
            JsonParser parser = new JsonParser();
            JsonElement json = parser.parse(EntityUtils.toString(entity));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println("");
            System.out.println("Respuesta recibida: ");
            System.out.println(gson.toJson(json));
            return json;

        } // Display errors if they occur.
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    // Realizamos preguntas en bucle a la BBDD de conocimientos recibida como parámetro
    public static void query_QnA_kb(String kb_id, String userQuestion) {

        QnAMakerRuntimeClient runtime_client = QnAMakerRuntimeManager.authenticate(QNA_ENDPOINT_KEY).withRuntimeEndpoint(QNA_ENDPOINT);

        // Realizamos la consulta a la Knowledge Base
        var query = (new QueryDTO()).withQuestion(userQuestion);

        // Recogemos el resultado de la consulta
        QnASearchResultList answers = runtime_client.runtimes().generateAnswer(kb_id, query);

        System.out.println("\033[34m Respuestas:");
        try {
            QnASearchResult result = answers.answers().get(0);
            System.out.println("\033[34m Fiabilidad de la respuesta (" + result.score() + ")");
            System.out.println(result.answer());

            // Comprobamos los posbles PROMPTS asociados a la pregunta
            if (result.context().prompts().size() > 0) {
                for (PromptDTO prompt : result.context().prompts()) {
                    System.out.println(prompt.qnaId() + ": " + prompt.displayText());
                }
                System.out.print("\033[34m Si quieres saber más indica el número entre paréntesis [Enter para seguir con la conversación]: ");
                String idPregunta = askUser();
                // Para un PROMPT seleccionado, lanzamos una pregunta con su qnaId asociado
                if (!idPregunta.equals("")) {
                    query = (new QueryDTO()).withQnaId(idPregunta);
                    answers = runtime_client.runtimes().generateAnswer(kb_id, query);

                    result = answers.answers().get(0);
                    System.out.println(result.answer());
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("\033[31m No se ha recibido ninguna respuesta desde la base de conocimiento de QnA");
        }

        System.out.println();

    }
}
