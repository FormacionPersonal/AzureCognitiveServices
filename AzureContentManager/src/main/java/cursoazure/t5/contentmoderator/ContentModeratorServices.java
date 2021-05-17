/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cursoazure.t5.contentmoderator;

import com.google.gson.*;

import com.microsoft.azure.cognitiveservices.vision.contentmoderator.*;
import com.microsoft.azure.cognitiveservices.vision.contentmoderator.models.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

class ContentModeratorInfo {
    public Boolean isAdult;
    public Double adultScore;
    public Boolean isRacy;
    public Double racyScore;
    public Boolean isInList;
    public int imageInListTag;
    public String imageInListLabel;

    public ContentModeratorInfo() {
        isAdult = false;
        isRacy = false;
        isInList = false;
    }        
}
        
/**
 *
 * @author VicenteMartínez
 */
public class ContentModeratorServices {

    // Content Moderator service configuration
    public static String contentModeratorSubscriptionKey = "f2ba689af18b4521b2c61ee395fcdb55";
    public static String contentModeratorrEndPoint = "https://t6cursocefirevmmcontentmoderator.cognitiveservices.azure.com/";
    
    public static String reviewSubscriptionKey = "f4adfc08d6964e248ef2fd9d8778e678";
    public static String reviewEndPoint = "https://francecentral.api.cognitive.microsoft.com/";
    public static String reviewTeam = "t6cursocefirevmm";

    // Tiempo de espera entre operaciones para evitar un fallo de operaciones máximas permitidas en la capa gratuita
    public static long freeTierOperationsPerSecond = 2000;

    public static String contentIMAGE = "https://moderatorsampleimages.blob.core.windows.net/samples/sample2.jpg";
    public static String contentTEXT = "La direción IP deel serviDor es 172.34.21.23 (para cualquier poblema escribir al fucking puto master de Administrador: admin@acme.com o buscarlo en Facebook)";
    
    // Objeto ContentModeratorInfo para guardar la información de Evaluate y Match y 
    // posteriormente usarla en la creación de la Revisión
    private static ContentModeratorInfo contentModeratorInfo = new ContentModeratorInfo();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {

                // Change default console output to UTF-8
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
        }

        // Autenticamos el cliente
        ContentModeratorClient contentModeratorClient = ContentModeratorManager.authenticate(AzureRegionBaseUrl.fromString(contentModeratorrEndPoint), contentModeratorSubscriptionKey);
        ContentModeratorClient contentReviewClient = ContentModeratorManager.authenticate(AzureRegionBaseUrl.fromString(reviewEndPoint), reviewSubscriptionKey);

        Thread.sleep(freeTierOperationsPerSecond);

        //moderarTexto(contentModeratorClient);
        moderarImagenes(contentModeratorClient);
        
        Thread.sleep(freeTierOperationsPerSecond);

        revisarImagenes(contentReviewClient);                
    }

    public static void revisarImagenes(ContentModeratorClient contentReviewClient) throws InterruptedException {
        String listaId = null;
        
        // Preparamos la revisión
        ArrayList<CreateReviewBodyItemMetadataItem> metadata = new ArrayList<>();
        metadata.add(new CreateReviewBodyItemMetadataItem().withKey("a").withValue(contentModeratorInfo.isAdult.toString()));
        metadata.add(new CreateReviewBodyItemMetadataItem().withKey("r").withValue(contentModeratorInfo.isRacy.toString()));
        metadata.add(new CreateReviewBodyItemMetadataItem().withKey("adultContentScore").withValue(contentModeratorInfo.adultScore.toString()));
        metadata.add(new CreateReviewBodyItemMetadataItem().withKey("racyContentScore").withValue(contentModeratorInfo.racyScore.toString()));
        metadata.add(new CreateReviewBodyItemMetadataItem().withKey("isInList").withValue(contentModeratorInfo.isInList.toString()));
        if (contentModeratorInfo.isInList) {
            metadata.add(new CreateReviewBodyItemMetadataItem().withKey("imageListLabel").withValue(contentModeratorInfo.imageInListLabel));
            metadata.add(new CreateReviewBodyItemMetadataItem().withKey("imageListTag").withValue(String.valueOf(contentModeratorInfo.imageInListTag)));
        }
            
        CreateReviewBodyItem revision = new CreateReviewBodyItem()
                .withType("image")
                .withContent(contentIMAGE)
                .withContentId("sample1.jpg")
                .withCallbackEndpoint(null)
                .withMetadata(metadata);

        List<String> listaIdRevision = contentReviewClient.reviews().createReviews()
                .withTeamName(reviewTeam)
                .withUrlContentType("application/json")
                .withCreateReviewBody( new ArrayList<CreateReviewBodyItem>() { { add(revision); } } )
                .execute();
        
        System.out.println("\nRealizar la validación manual de la revisión en el portal de Content Moderator.");
        System.out.println("El proceso comprobará cuándo la revisión está hecha y continuará automáticamente para mostrar los resultados...");

        Review infoRevision = null;
        do {
            // Esperamos 
            Thread.sleep(freeTierOperationsPerSecond*2);                            
            // Consultamos la revisión
            infoRevision = contentReviewClient.reviews().getReview(reviewTeam, listaIdRevision.get(0));
        } while (!infoRevision.status().equals("Complete"));
        
        if (infoRevision.status().equals("Complete")) {
            System.out.println("Etiquetado de la revisión:");
            for (KeyValuePair tag: infoRevision.reviewerResultTags()) {
                System.out.println(tag.key() + "=" + tag.value());
            }
        }
        
                
    }

    
    public static void moderarImagenes(ContentModeratorClient contentModeratorClient) throws InterruptedException {
        String listaId = null;

        try {
            // Creamos la lista de imagenes
            System.out.println("Creando la lista de imágenes... (el proceso tarda unos segundos por las esperas para poder usar la free tier)");
            listaId = crearListaImagenes(contentModeratorClient);
            System.out.println("\t--> creada la lista: " + listaId);

            Thread.sleep(freeTierOperationsPerSecond);

            // Evaluamos el contenido de una imagen
            BodyModelModel imageUrl = new BodyModelModel()
                    .withDataRepresentation("uRL")
                    .withValue(contentIMAGE);
            Evaluate evaluatedImage = contentModeratorClient.imageModerations().evaluateUrlInput()
                    .withContentType("application/json")
                    .withImageUrl(imageUrl)
                    .execute();

            // Mostramos el resultado de la evaluación
            System.out.println("Contenido de adultos: " + evaluatedImage.isImageAdultClassified() + " - " + evaluatedImage.adultClassificationScore());
            System.out.println("Contenido sugerente: " + evaluatedImage.isImageRacyClassified() + " - " + evaluatedImage.racyClassificationScore());
            
            // Guardamos la información para la revisión
            contentModeratorInfo.isAdult = evaluatedImage.isImageAdultClassified();
            contentModeratorInfo.adultScore = evaluatedImage.adultClassificationScore();
            contentModeratorInfo.isRacy = evaluatedImage.isImageRacyClassified();
            contentModeratorInfo.racyScore = evaluatedImage.racyClassificationScore();

            Thread.sleep(freeTierOperationsPerSecond);

            // Buscamos la imagen en la lista
            MatchResponse matchedImage = contentModeratorClient.imageModerations().matchUrlInput()
                    .withContentType("application/json")
                    .withImageUrl(imageUrl)
                    .execute();

            // Mostramos el resultado del matching
            if (matchedImage.isMatch()) {
                contentModeratorInfo.isInList = true;
                for (Match coincidencia : matchedImage.matches()) {
                    System.out.println("Descripción: " + coincidencia.label() + " - Categoría: " + coincidencia.tags().get(0));
                    contentModeratorInfo.imageInListLabel = coincidencia.label();
                    contentModeratorInfo.imageInListTag = coincidencia.tags().get(0);
                }
            }

            Thread.sleep(freeTierOperationsPerSecond);

            // Borramos la lista de imagenes
            System.out.println("Eliminando lista:" + listaId);
            eliminarListaImagenes(contentModeratorClient, listaId);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            // Eliminamos recursos para poder seguir usando la capa gratuita
            Thread.sleep(freeTierOperationsPerSecond);
            vaciarListasImagenes(contentModeratorClient);
        }

    }

    public static String crearListaImagenes(ContentModeratorClient client) throws InterruptedException {
        // Creamos la lista
        ImageList lista = client.listManagementImageLists().create("application/json", new BodyModel().withName("imagenes_prohibidas"));
        String listaId = lista.id().toString();

        Thread.sleep(freeTierOperationsPerSecond);

        // Añadimos las imágenes
        BodyModelModel imageUrl = new BodyModelModel()
                .withDataRepresentation("URL")
                .withValue("https://moderatorsampleimages.blob.core.windows.net/samples/sample1.jpg");
        client.listManagementImages().addImageUrlInput()
                .withListId(listaId)
                .withContentType("application/json")
                .withImageUrl(imageUrl)
                .withTag(101)
                .withLabel("Grupo de gente en el lago")
                .execute();

        Thread.sleep(freeTierOperationsPerSecond);

        imageUrl = new BodyModelModel()
                .withDataRepresentation("URL")
                .withValue("https://moderatorsampleimages.blob.core.windows.net/samples/sample3.png");
        client.listManagementImages().addImageUrlInput()
                .withListId(listaId)
                .withContentType("application/json")
                .withImageUrl(imageUrl)
                .withTag(101)
                .withLabel("sample3.png")
                .execute();

        Thread.sleep(freeTierOperationsPerSecond);

        // Refrescamos el índice de la lista
        client.listManagementImageLists().refreshIndexMethod(listaId);

        return listaId;
    }

    public static void eliminarListaImagenes(ContentModeratorClient client, String listaId) {
        // Eliminamos la lista
        // Sólo para poder ejecutar el código varias veces y no duplicar la 
        // misma lista en las diferentes ejecuciones
        client.listManagementImageLists().delete(listaId);
    }

    public static void vaciarListasImagenes(ContentModeratorClient client) throws InterruptedException {
        List<ImageList> listasCreadas = client.listManagementImageLists().getAllImageLists();
        for (ImageList listaCreada : listasCreadas) {
            Thread.sleep(freeTierOperationsPerSecond);
            System.err.println("\n\nERROR: Vaciando todas las listas:" + listaCreada.id());
            eliminarListaImagenes(client, listaCreada.id().toString());

        }
    }

    public static void moderarTexto(ContentModeratorClient contentModeratorClient) throws InterruptedException {
        String listaId = null;
        Screen moderatedTEXT = null;

        try {
            // Creamos la lista de palabras
            System.out.println("Creando la lista de palabras... (el proceso tarda unos segundos por las esperas para poder usar la free tier)");
            listaId = crearListaPalabras(contentModeratorClient);
            System.out.println("\t--> creada la lista: " + listaId);

            Thread.sleep(freeTierOperationsPerSecond);

            // Invocamos el método del API para el análisis del texto
            moderatedTEXT = contentModeratorClient.textModerations()
                    .screenText()
                    .withTextContentType("text/plain")
                    .withTextContent(contentTEXT.getBytes())
                    .withLanguage("spa")
                    .withPII(Boolean.TRUE)
                    .withAutocorrect(Boolean.TRUE)
                    .withListId(listaId)
                    .withClassify(Boolean.FALSE)
                    .execute();

            // Mostrar texto autocorregido
            System.out.println("Texto corregido: " + moderatedTEXT.autoCorrectedText());

            // Mostrar términos encontrados (de la lista o palabras inadecuadas)
            if (moderatedTEXT.terms().size() != 0) {
                System.out.println("Palabras no adecuadas: ");
                for (DetectedTerms termino : moderatedTEXT.terms()) {
                    System.out.println(termino.term());
                }
            }

            // Borramos la lista de palabras
            System.out.println("Eliminando lista:" + listaId);
            eliminarListaPalabras(contentModeratorClient, listaId);

        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            // Eliminamos recursos para poder seguir usando la capa gratuita
            Thread.sleep(freeTierOperationsPerSecond);
            vaciarListasPalabras(contentModeratorClient);
        }
    }

    public static String crearListaPalabras(ContentModeratorClient client) throws InterruptedException {
        // Creamos la lista
        TermList lista = client.listManagementTermLists().create("application/json", new BodyModel().withName("palabras_prohibidas"));
        String listaId = lista.id().toString();

        Thread.sleep(freeTierOperationsPerSecond);

        // Añadimos los términos
        client.listManagementTerms().addTerm(listaId, "Facebook", "spa");
        Thread.sleep(freeTierOperationsPerSecond);
        client.listManagementTerms().addTerm(listaId, "Instagram", "spa");
        Thread.sleep(freeTierOperationsPerSecond);

        // Refrescamos el índice de la lista
        client.listManagementTermLists().refreshIndexMethod(listaId, "spa");

        return listaId;
    }

    public static void eliminarListaPalabras(ContentModeratorClient client, String listaId) {
        // Eliminamos la lista
        // Sólo para poder ejecutar el código varias veces y no duplicar la 
        // misma lista en las diferentes ejecuciones
        client.listManagementTermLists().delete(listaId);
    }

    public static void vaciarListasPalabras(ContentModeratorClient client) throws InterruptedException {
        List<TermList> listasCreadas = client.listManagementTermLists().getAllTermLists();
        for (TermList listaCreada : listasCreadas) {
            Thread.sleep(freeTierOperationsPerSecond);
            System.err.println("\n\nERROR: Vaciando todas las listas:" + listaCreada.id());
            eliminarListaPalabras(client, listaCreada.id().toString());

        }
    }

}
