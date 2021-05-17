/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cursoazure.t2.face;

import com.microsoft.azure.cognitiveservices.vision.faceapi.*;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.*;
import java.util.*;

/**
 *
 * @author VicenteMartínez
 */
public class AzureFaceDetectInfo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        final AzureRegions myRegion = AzureRegions.SOUTHCENTRALUS;
        final String SUBSCRIPTION_KEY="3330b58502d9414dbe2af804a3430349";
        final String ENDPOINT = "https://t2cursocefirevmmface.cognitiveservices.azure.com/";
        final String IMAGE_URL = "https://img.fixthephoto.com/blog/images/gallery/news_image_404.jpg";


        // Creación y conexión con el servicio FACE
        FaceAPI client = FaceAPIManager.authenticate(myRegion, SUBSCRIPTION_KEY);
        
        // Detección de caras en una imagen (Face-Detect)
        System.out.println("--Detección de caras--");
        
        // Preparamos la solicitud
        DetectWithUrlOptionalParameter atributos = new DetectWithUrlOptionalParameter();
        atributos.withReturnFaceId(Boolean.TRUE);     
        //atributos.returnFaceLandmarks();
        atributos.withReturnFaceAttributes(Arrays.asList (FaceAttributeType.GENDER, FaceAttributeType.AGE, FaceAttributeType.EMOTION));
        
        List<DetectedFace> facesList = client.faces().detectWithUrl(IMAGE_URL, atributos);
        System.out.println("Detected face ID(s) from URL image: " + IMAGE_URL  + " :");
        
        for (DetectedFace face: facesList) {
            System.out.println("FACE Id: " + face.faceId());
            System.out.println("--> RECTANGLE: {(top:" + face.faceRectangle().top() + ", left:" + face.faceRectangle().left() + "),(height:" + face.faceRectangle().height() + ", width:" + face.faceRectangle().width() + ")}");
            System.out.println("--> GENDER: " + face.faceAttributes().gender().toString());
            System.out.println("--> AGE: " + face.faceAttributes().age().toString());
            System.out.println("--> EMOTION: " + getBigger(face.faceAttributes().emotion()));
        }
    }

    private static String getBigger(Emotion emotion) {    
        double maxValue = 0;
        String maxEmotion = "NO EMOTION DETECTED";
        
        if (emotion.anger() > maxValue) {maxEmotion = "ANGER"; maxValue = emotion.anger();}
        if (emotion.contempt() > maxValue) {maxEmotion = "CONTEMPT"; maxValue = emotion.contempt();}
        if (emotion.disgust() > maxValue) {maxEmotion = "DISGUST"; maxValue = emotion.disgust();}
        if (emotion.fear() > maxValue) {maxEmotion = "FEAR"; maxValue = emotion.fear();}
        if (emotion.happiness()> maxValue) {maxEmotion = "HAPPINESS"; maxValue = emotion.happiness();}
        if (emotion.neutral()> maxValue) {maxEmotion = "NEUTRAL"; maxValue = emotion.neutral();}
        if (emotion.sadness()> maxValue) {maxEmotion = "SADNESS"; maxValue = emotion.sadness();}
        if (emotion.surprise()> maxValue) {maxEmotion = "SURPRISE"; maxValue = emotion.surprise();}        
        
        return maxEmotion + " (with intensity: " + maxValue + ")";
    }
    
}
