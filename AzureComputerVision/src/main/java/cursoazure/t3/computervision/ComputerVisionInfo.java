/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cursoazure.t3.computervision;

import com.microsoft.azure.cognitiveservices.vision.computervision.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VicenteMartínez
 */
public class ComputerVisionInfo {

    static BufferedWriter htmlContent = null;
    static ComputerVisionClient computerVisionClient;
    static String imageBaseUrl = "https://raw.githubusercontent.com/Azure-Samples/cognitive-services-sample-data-files/master/ComputerVision/Images/";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        // Imágenes de ejemplo a utilizar en el código                
        String remoteImage1 = "landmark.jpg";
        String remoteImage2 = "landmark.png";
        String remoteImage3 = "cheese_clipart.png";
        String remoteImage4 = "printed_text.jpg";
        String remoteImage5 = "MultiLingual.png";

        // Inicializamos el contenido del hTML
        htmlContent = new BufferedWriter(new FileWriter("ComputerVision.html"));
        htmlContent.write("<!doctype html>\n<html lang=\"en-EN\">\n<head>\n<meta charset=\"utf-8\"></head>\n<body>\n<h1>Azure Cognitive Services - Computer Vision</h1>\n");
        htmlContent.append("<h2>Imágenes de prueba</h2>\n");
        htmlContent.append("<img src=\"" + imageBaseUrl + remoteImage1 + "\" width=200 title=\"" + remoteImage1 + "\" alt=\"" + remoteImage1 + "\"/>\n");
        htmlContent.append("<img src=\"" + imageBaseUrl + remoteImage2 + "\" width=200 title=\"" + remoteImage2 + "\" alt=\"" + remoteImage2 + "\"/>\n");
        htmlContent.append("<img src=\"" + imageBaseUrl + remoteImage3 + "\" width=200 title=\"" + remoteImage3 + "\" alt=\"" + remoteImage3 + "\"/>\n");
        htmlContent.append("<img src=\"" + imageBaseUrl + remoteImage4 + "\" width=200 title=\"" + remoteImage4 + "\" alt=\"" + remoteImage4 + "\"/>\n");
        htmlContent.append("<img src=\"" + imageBaseUrl + remoteImage5 + "\" width=200 title=\"" + remoteImage5 + "\" alt=\"" + remoteImage5 + "\"/>\n");

        System.out.println("-----------------------------------------------");
        System.out.println("CONEXIÓN CON EL SERVICIO COMPUTER VISION");
        System.out.println();

        /**
         * AUTHENTICATE Create a client that authorizes your Computer Vision
         * subscription key and region.
         */
        String subscriptionKey = "820dbeebbfcb405d983fa6304eb2002d"; // System.getenv("COMPUTER_VISION_SUBSCRIPTION_KEY");
        if (subscriptionKey == null) {
            System.err.println("\n\nLa clave del servicio introducida no es válida."
                    + "\n**Por favor revísela en el recurso creado el portal.azure.com.**\n");
            System.exit(0);
        }

        String endpoint = "https://t2cursocefirevmmcomputervision.cognitiveservices.azure.com/"; //System.getenv("COMPUTER_VISION_ENDPOINT");
        if (endpoint == null) {
            System.err.println("\n\nLa URL de acceso al servicio no es válida."
                    + "\n**Por favor revísela en el recurso creado el portal.azure.com.**\n");
            System.exit(0);
        }

        computerVisionClient = ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
        System.out.println();

        System.out.println("-----------------------------------------------");
        System.out.println("FRASE DESCRIPTIVA DE LA IMAGEN");
        System.out.println();
        htmlContent.append("<h2>Frase descriptiva de la imagen</h2>\n");
        obtenerDescripcion(remoteImage1, "en");
        obtenerDescripcion(remoteImage1, "es");
        obtenerDescripcion(remoteImage2, "en");
        obtenerDescripcion(remoteImage3, "en");
        System.out.println();

        System.out.println("-----------------------------------------------");
        System.out.println("ETIQUETAS DE LA IMAGEN");
        System.out.println();
        htmlContent.append("<h2>Etiquestas de la imagen</h2>\n");
        obtenerEtiquetas(remoteImage1, "en");
        obtenerEtiquetas(remoteImage1, "es");
        obtenerEtiquetas(remoteImage2, "en");
        obtenerEtiquetas(remoteImage3, "en");
        System.out.println();

        System.out.println("-----------------------------------------------");
        System.out.println("COLORES DE LA IMAGEN");
        System.out.println();
        htmlContent.append("<h2>Información de color de la imagen</h2>\n");
        obtenerColores(remoteImage1);
        obtenerColores(remoteImage2);
        obtenerColores(remoteImage3);
        System.out.println();

        System.out.println("-----------------------------------------------");
        System.out.println("MINIATURAS DE LA IMAGEN");
        System.out.println();
        htmlContent.append("<h2>Miniaturas de la imagen</h2>\n");
        obtenerMiniaturas(remoteImage2, "miniatura2_h.png", 200, 100);
        obtenerMiniaturas(remoteImage2, "miniatura2_v.png", 100, 200);
        System.out.println();

        System.out.println("-----------------------------------------------");
        System.out.println("RECONOCIMIENTO DE TEXTO EN LA IMAGEN");
        System.out.println();
        htmlContent.append("<h2>Reconocimiento de texto en la imagen</h2>\n");
        obtenerTexto(remoteImage4);
        obtenerTexto(remoteImage5);
        System.out.println();

        htmlContent.append("</body>\n</html>\n");
        htmlContent.close();
    }

    private static void obtenerDescripcion(String remoteImage, String lang) {

        // Acceder a la descripción de una imagen remota en inglés
        ImageDescription analysisRemote = computerVisionClient.computerVision().describeImage()
                .withUrl(imageBaseUrl + remoteImage)
                .withLanguage(lang)
                .withMaxCandidates(1)
                .execute();
        try {
            ImageCaption caption = analysisRemote.captions().get(0);
            System.out.println(remoteImage);
            htmlContent.append("<a href=\"" + imageBaseUrl + remoteImage + "\"> " + remoteImage + "</a>\n");
            String salida = "Descripción(" + lang + "): \"" + caption.text() + "\" con una confianza del " + Math.round(caption.confidence() * 100.0) / 100.0;
            System.out.println(salida);
            htmlContent.append("<p>" + salida + "</p>");
        } catch (Exception e) {
            System.err.println("\n\nNo se ha encontrado una descripción para la imagen\n");
        }
    }

    private static void obtenerEtiquetas(String remoteImage, String lang) {

        // Obtener las etiquetas de una imagen remota
        TagResult analysisRemote = computerVisionClient.computerVision().tagImage()
                .withUrl(imageBaseUrl + remoteImage)
                .withLanguage(lang)
                .execute();

        try {
            System.out.println(remoteImage);
            htmlContent.append("<a href=\"" + imageBaseUrl + remoteImage + "\"> " + remoteImage + "</a>\n");
            htmlContent.append("<p>");
            for (ImageTag tag : analysisRemote.tags()) {
                String salida = tag.name() + "(" + Math.round(tag.confidence() * 100.0) / 100.0 + ") ";
                System.out.print(salida);
                htmlContent.append(salida);
            }
            htmlContent.append("</p>");
            System.out.println();
        } catch (Exception e) {
            System.err.println("\n\nNo se han encontrado etiquetas para la imagen\n");
        }
    }

    private static void obtenerColores(String remoteImage) {

        // Detectar características generales de una imagen remota
        List<VisualFeatureTypes> features = new ArrayList<>();
        features.add(VisualFeatureTypes.COLOR);

        ImageAnalysis analysisRemote = computerVisionClient.computerVision().analyzeImage()
                .withUrl(imageBaseUrl + remoteImage)
                .withVisualFeatures(features)
                .execute();

        try {
            ColorInfo color = analysisRemote.color();
            System.out.println(remoteImage);
            htmlContent.append("<a href=\"" + imageBaseUrl + remoteImage + "\"> " + remoteImage + "</a>\n");
            String salida = "Color de énfasis de la imagen(AccentColor): " + color.accentColor();
            System.out.println(salida);
            htmlContent.append("<p>" + salida + "</p>");
        } catch (Exception e) {
            System.err.println("\n\nNo se ha encontrado información de color para la imagen\n");
        }
    }

    private static void obtenerMiniaturas(String remoteImage, String localImage, int thumbWidth, int thumbHeight) {
        InputStream horizontalIS = computerVisionClient.computerVision().generateThumbnail().
                withWidth(thumbWidth).
                withHeight(thumbHeight).
                withUrl(imageBaseUrl + remoteImage).
                withSmartCropping(Boolean.TRUE).
                execute();

        try {
            // Guardar la miniatrua en un archivo
            java.nio.file.Files.copy(
                    horizontalIS,
                    new File(localImage).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            htmlContent.append("<img src=\"" + localImage + "\" title=\"" + localImage + "\" alt=\"" + localImage + "\"/>\n");
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            System.err.println("\n\nNo se ha podido guardar el archivo " + localImage + "en disco."
                    + "\n**Por favor revíse los permisos y la ubicación de almacenamiento.**\n");

        }
    }

    private static void obtenerTexto(String remoteImage) {
// Recognize printed text in remote image
        OcrResult ocrResultRemote = computerVisionClient.computerVision().recognizePrintedText()
                .withDetectOrientation(true)
                .withUrl(imageBaseUrl + remoteImage)
                .withLanguage(OcrLanguages.EN)
                .execute();

        try {

            System.out.println(remoteImage);
            htmlContent.append("<a href=\"" + imageBaseUrl + remoteImage + "\"> " + remoteImage + "</a>\n");

            for (OcrRegion reg : ocrResultRemote.regions()) {
                // Get one line in the text block                
                for (OcrLine line : reg.lines()) {
                    htmlContent.append("<p>");
                    for (OcrWord word : line.words()) {
                        System.out.print(word.text() + " ");
                        htmlContent.append(word.text() + " ");
                    }
                    System.out.println();
                    htmlContent.append("</p>");
                }
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.err.println("\n\nNo se ha podido acceder al texto del archivo " + remoteImage + "en disco."
                    + "\n**Por favor revíse la imagen.**\n");

        }
    }
}
