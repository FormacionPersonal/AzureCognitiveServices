/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cursoazure.t3.customvision;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

//import com.google.common.io.ByteStreams;

import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.ImagePrediction;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.Prediction;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.CustomVisionPredictionClient;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.CustomVisionPredictionManager;

/**
 *
 * @author VicenteMartínez
 */
public class CustomVisionClasify {

    static CustomVisionPredictionClient customVisionPredictionClient;
    static String imageBaseDirectory = "testImages/";
    static String projectId = "7abb1556-c42b-4263-a853-72406e95764c";
    static String publishedName = "Iteration1";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Imágenes de ejemplo a utilizar en el código                
        String localImage1 = "bicicleta_antigua.jpg";
        String localImage2 = "smart.jpg";
        String localImage3 = "ciclomotor.jpg";
        String localImage4 = "quad.jpg";
        String localImage5 = "vespa_azul.jpg";
        
        System.out.println("-----------------------------------------------");
        System.out.println("CONEXIÓN CON EL SERVICIO CUSTOM VISION");
        System.out.println();

        /**
         * AUTHENTICATE Create a client that authorizes your Computer Vision
         * subscription key and region.
         */
        String predictionKey = "2c850e9db2f146648ecd5752584a245f"; // System.getenv("COMPUTER_VISION_SUBSCRIPTION_KEY");
        if (predictionKey == null) {
            System.err.println("\n\nLa clave del servicio introducida no es válida."
                    + "\n**Por favor revísela en el recurso creado el portal.azure.com.**\n");
            System.exit(0);
        }

        String endpoint = "https://t3cursocefirevmmcustomvision.cognitiveservices.azure.com/"; //System.getenv("COMPUTER_VISION_ENDPOINT");
        if (endpoint == null) {
            System.err.println("\n\nLa URL de acceso al servicio no es válida."
                    + "\n**Por favor revísela en el recurso creado el portal.azure.com.**\n");
            System.exit(0);
        }

        customVisionPredictionClient = CustomVisionPredictionManager
                .authenticate("https://{endpoint}/customvision/v3.0/prediction/", predictionKey)
                .withEndpoint(endpoint);

        testImage(localImage1);
        testImage(localImage2);
        testImage(localImage3);
        testImage(localImage4);
        testImage(localImage5);
    }

    // Realizar la predicción sobre la imagen recibida
    private static void testImage(String localImage) {

        byte[] testImage = readLocalImage(localImage);

        // predict
        ImagePrediction results = customVisionPredictionClient.predictions()
                .classifyImage()
                .withProjectId(UUID.fromString(projectId))
                .withPublishedName(publishedName)
                .withImageData(testImage)
                .execute();

        System.out.println("Imagen: " + localImage);
        for (Prediction prediction : results.predictions()) {
            System.out.println("  >> Etiqueta: " + prediction.tagName() + " - Probabilidad: " +  Math.round(prediction.probability() * 100.0) / 100.);
        }
    }

    // Cargar la imagen de disco para ser procesada por el API de predicción
    private static byte[] readLocalImage(String fileName) {
        try {            
            //return ByteStreams.toByteArray(new FileInputStream(new File(imageBaseDirectory+fileName)));
            return new FileInputStream(new File(imageBaseDirectory+fileName)).readAllBytes();
        } catch (Exception e) {
            System.err.println("Error when reading the local image from disk: " + imageBaseDirectory + fileName);
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
