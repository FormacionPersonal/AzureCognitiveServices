/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cursoazure.t7.bingsearch;

import com.microsoft.bing.websearch.implementation.WebSearchClientImpl;
import com.microsoft.bing.websearch.models.Freshness;
import com.microsoft.bing.websearch.models.ImageObject;
import com.microsoft.bing.websearch.models.NewsArticle;
import com.microsoft.bing.websearch.models.Query;
import com.microsoft.bing.websearch.models.SafeSearch;
import com.microsoft.bing.websearch.models.SearchResponse;
import com.microsoft.bing.websearch.models.TextFormat;
import com.microsoft.bing.websearch.models.VideoObject;
import com.microsoft.bing.websearch.models.WebPage;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import okhttp3.*;
import okhttp3.OkHttpClient.Builder;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author VicenteMartínez
 */
public class BingSearchServices {

    // Content Moderator service configuration
    public static String bingSearchv7SubscriptionKey = "b8f487bea0be4ee9b01936a461404116";
    public static String bingSearchv7EndPoint = "https://api.bing.microsoft.com/v7.0";
        
    public static String bingSearchText = "Voris Yonson";

    public static void main(String[] args) throws IOException {

        String htmlContent;
        File htmlTemplate, htmlOutput;
        
        
        // Change default console output to UTF-8
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
        }

        // Inicializamos el contenido del HTML
        htmlTemplate = new File("BingSearchTemplate.html");
        htmlContent = FileUtils.readFileToString(htmlTemplate);    
    
        // Autenticamos el cliente        
        WebSearchClientImpl bingSearchClient = obtenerCredenciales();

        // Ejecución de la búsqueda
        SearchResponse bingSearchResponse = bingSearchClient.webs().search(bingSearchText, null, null, null, null, null, null, null, null, 5, null, "es-ES", null, null, null, SafeSearch.OFF, "es", Boolean.TRUE, TextFormat.RAW);
        
        // Resultados de contexto       
        System.out.println ("Se muestran resultados de " + bingSearchResponse.queryContext().alteredQuery());
        System.out.println ("¿Realmente querías buscar " + bingSearchResponse.queryContext().alterationOverrideQuery() + "?");
        htmlContent = htmlContent.replace("$bingSearchText$", bingSearchText);
        htmlContent = htmlContent.replace("$alteredQuery$", bingSearchResponse.queryContext().alteredQuery());
        htmlContent = htmlContent.replace("$alterationOverrideQuery$", bingSearchResponse.queryContext().alterationOverrideQuery());

        // Resultados de webs
        if (bingSearchResponse.webPages().value().size() > 0) {
            System.out.println(bingSearchResponse.webPages().value().size() + " páginas web encontradas: ");
            for (WebPage webPage: bingSearchResponse.webPages().value()) {
                System.out.println(">> " + webPage.name() + "\n\t >> " + webPage.url() + "\n\t >> " + webPage.snippet());
            }
            htmlContent = htmlContent.replace("$webpageName$", bingSearchResponse.webPages().value().get(0).name());            
            htmlContent = htmlContent.replace("$webpageUrl$", bingSearchResponse.webPages().value().get(0).url());            
            htmlContent = htmlContent.replace("$webpageSnippet$", bingSearchResponse.webPages().value().get(0).snippet());            
            htmlContent = htmlContent.replace("$webpageError$", "");            
        } else {
            System.out.println("No se ha encontrado ninguna web para la búsqueda realizada.");
            htmlContent = htmlContent.replace("$webpageError$", "No se ha encontrado ninguna web para la búsqueda realizada.");
        }
            
        // Resultados de imágenes
        if (bingSearchResponse.images().value().size() > 0) {
            System.out.println(bingSearchResponse.images().value().size() + " imágenes encontradas: ");
            for (ImageObject imageObject: bingSearchResponse.images().value()) {
                System.out.println(">> " + imageObject.name() + "\n\t >> " + imageObject.contentUrl() + "\n\t >> " + imageObject.hostPageUrl() + "\n\t >> " + imageObject.thumbnailUrl());
            }
            htmlContent = htmlContent.replace("$imageName$", bingSearchResponse.images().value().get(0).name());                        
            htmlContent = htmlContent.replace("$imageThumbnail$", bingSearchResponse.images().value().get(0).thumbnailUrl());                        
            htmlContent = htmlContent.replace("$imageUrl$", bingSearchResponse.images().value().get(0).contentUrl());                        
            htmlContent = htmlContent.replace("$imageError$", "");                        
        } else {
            System.out.println("No se ha encontrado ninguna imagen para la búsqueda realizada.");
            htmlContent = htmlContent.replace("$imageError$", "No se ha encontrado ninguna image para la búsqueda realizada.");
        }

        // Resultados de vídeos
        if (bingSearchResponse.videos().value().size() > 0) {
            System.out.println(bingSearchResponse.videos().value().size() + " vídeos encontrados: ");
            for (VideoObject videoObject: bingSearchResponse.videos().value()) {
                System.out.println(">> " + videoObject.name() + "\n\t >> " + videoObject.contentUrl() + "\n\t >> " + videoObject.hostPageUrl() + "\n\t >> " + videoObject.thumbnailUrl() + "\n\t >> " + videoObject.motionThumbnailUrl() + "\n\t >> " + videoObject.embedHtml() + "\n\t >> " + videoObject.viewCount());
            }
            htmlContent = htmlContent.replace("$videoContent$", bingSearchResponse.videos().value().get(0).embedHtml());                        
            htmlContent = htmlContent.replace("$videoName$", bingSearchResponse.videos().value().get(0).name());                        
            htmlContent = htmlContent.replace("$videoError$", "");                        
        } else {
            System.out.println("No se ha encontrado ningún vídeo para la búsqueda realizada.");
            htmlContent = htmlContent.replace("$videoError$", "No se ha encontrado ninguna vídeo para la búsqueda realizada.");            
        }

/*        
        // Resultados de noticias
        if (bingSearchResponse.news().value().size() > 0) {
            System.out.println(bingSearchResponse.news().value().size() + " noticias encontradas: ");
            for (NewsArticle newsArticle: bingSearchResponse.news().value()) {
                System.out.println(">> " + newsArticle.name() + "\n\t >> " + newsArticle.description() + "\n\t >> " + newsArticle.url()+ "\n\t >> " + newsArticle.thumbnailUrl());
            }
        } else {
            System.out.println("No se ha encontrado ninguna noticia para la búsqueda realizada.");
        }
*/
        // Búsquedas relacionadas
        if (bingSearchResponse.relatedSearches().value().size() > 0) {
            System.out.println(bingSearchResponse.relatedSearches().value().size() + " búsquedas relacionadas encontradas: ");
            for (Query relatedQuery: bingSearchResponse.relatedSearches().value()) {
                System.out.println(">> " + relatedQuery.text()+ "\n\t >> " + relatedQuery.webSearchUrl());
            }
        } else {
            System.out.println("No se ha encontrado ningúa búsqueda relacionada para la búsqueda realizada.");
        }        
        
        // Guardamos el contenido en el HTML
        htmlOutput = new File("BingSearch.html");
        FileUtils.writeStringToFile(htmlOutput, htmlContent);        
    }

    public static WebSearchClientImpl obtenerCredenciales() {
        ServiceClientCredentials credentials = new ServiceClientCredentials() {
            @Override
            public void applyCredentialsFilter(Builder builder) {
                builder.addNetworkInterceptor(
                        new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = null;
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder();
                        requestBuilder.addHeader("Ocp-Apim-Subscription-Key", bingSearchv7SubscriptionKey);
                        request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                }
                );
            }
        };
        WebSearchClientImpl client = new WebSearchClientImpl(bingSearchv7EndPoint, credentials);
        
        return client;
    }
}
