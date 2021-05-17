/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cursoazure.t2.face;

import com.microsoft.azure.cognitiveservices.vision.faceapi.FaceAPI;
import com.microsoft.azure.cognitiveservices.vision.faceapi.FaceAPIManager;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.AddPersonFaceFromUrlOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.AzureRegions;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.CreatePersonGroupPersonsOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.CreatePersonGroupsOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.DetectWithUrlOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.DetectedFace;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.IdentifyResult;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.Person;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.TrainingStatus;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.TrainingStatusType;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.VerifyResult;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author VicenteMartínez
 */
public class FaceDetectIdentify {
    public static void main(String[] args) {
        // TODO code application logic here
        final AzureRegions myRegion = AzureRegions.SOUTHCENTRALUS;
        final String SUBSCRIPTION_KEY="3330b58502d9414dbe2af804a3430349";
        final String ENDPOINT = "https://t2cursocefirevmmface.cognitiveservices.azure.com/";
        final String IMAGE_URL = "https://peru21.pe/resizer/8jJweJ9QH6ORWbUVKWfJ3l2008E=/980x0/smart/filters:format(jpeg):quality(75)/arc-anglerfish-arc2-prod-elcomercio.s3.amazonaws.com/public/GHKF7KXNWVDGFDOIYN3NO4NKMM.jpg";
        final String IMAGE1_URL = "https://sm.ign.com/t/ign_latam/screenshot/d/daenerys-t/daenerys-targaryen-in-season-2_v831.1080.jpg";
        final String IMAGE2_URL = "https://sm.ign.com/t/ign_latam/screenshot/d/daenerys-t/daenerys-targaryen-in-season-7_a33e.1080.jpg";        
        final String PERSONGROUP_ID = "practica2vmm";

        // Change default console output to UTF-8
        try {            
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {}

        // Creación y conexión con el servicio FACE
        FaceAPI client = FaceAPIManager.authenticate(myRegion, SUBSCRIPTION_KEY);
        
        // Verificación y Detección de caras en una imagen (Face-Verify & Face-Detect)
        System.out.println("\033[32m-- Identificación de caras --");
        
        // Paso 0 - Verificación de caras
        System.out.println("\033[34m   0.- Verificación de caras");
         // Detectar caras en las imágenes de prueba
        List<UUID> source1ID = detectFaces(client, IMAGE1_URL);
        List<UUID> source2ID = detectFaces(client, IMAGE2_URL);
        
        VerifyResult sameResult = client.faces().verifyFaceToFace(source1ID.get(0), source2ID.get(0));        
        if (sameResult.isIdentical()) {
            System.out.println("    >> Las imágenes de prueba pertenecen a la misma persona");
        } else {
            System.out.println("    >> Las imágenes de prueba no pertenecen a la misma persona");
            return;            
        }
        System.out.println("");
       
        
        // Paso 1 - Creación del grupo
        System.out.println("\033[34m   1.- Creación del grupo: " + PERSONGROUP_ID);
        try {
            client.personGroups().delete(PERSONGROUP_ID);
        } catch(Exception e) {}       
        client.personGroups().create(PERSONGROUP_ID, new CreatePersonGroupsOptionalParameter().withName("Games Of Thrones"));
        System.out.println("");
       
        // Paso 2 - Adición de personas al grupo            
        System.out.println("\033[34m   2.- Asginación de personas al grupo: " + client.personGroups().get(PERSONGROUP_ID).name());
        Person testPerson = client.personGroupPersons().create(PERSONGROUP_ID, new CreatePersonGroupPersonsOptionalParameter().withName("Daenerys Targaryen"));
        System.out.println("    >> Añadida persona: " + testPerson.personId());
        System.out.println("");
        
        // Paso 3 - Asignación de imágenes a las personas del grupo            
        System.out.println("\033[34m   3.- Asginación de imágenes a la persona: " + client.personGroupPersons().get(PERSONGROUP_ID, testPerson.personId()).name());
        client.personGroupPersons().addPersonFaceFromUrl(PERSONGROUP_ID, testPerson.personId(), IMAGE1_URL, null);
        System.out.println("    >> Añadida imagen: " + IMAGE1_URL);
        client.personGroupPersons().addPersonFaceFromUrl(PERSONGROUP_ID, testPerson.personId(), IMAGE2_URL, null);
        System.out.println("    >> Añadida imagen: " + IMAGE2_URL);
        System.err.println("");

        // Paso 4 - Entrenamiento del grupo de imágenes            
        System.out.println("\033[34m   4.- Entrenamiento del grupo");
        client.personGroups().train(PERSONGROUP_ID);

        // Bucle de espera a la finalización del entrenamiento
        while(true) {
            try {
                sleep(1000);
            } catch (InterruptedException e) { e.printStackTrace(); }
            
            // Get training status
            TrainingStatus status = client.personGroups().getTrainingStatus(PERSONGROUP_ID);
            if (status.status() == TrainingStatusType.SUCCEEDED) {
                System.out.println("    >> Training status: " + status.status());
                break;
            } else {
                System.out.println("    >> Training status: " + status.status());
            }
        }
        System.out.println("");
        
        // Paso 5 - Identificación de cara en una imagen
        System.out.println("\033[34m   5.- Detección de caras en una imagen");       
        List<UUID> detectedFaces = detectFaces(client, IMAGE_URL);
        System.out.println("");

        // Identificar caras 
        System.out.println("\033[34m   6.- Identificación de caras en el grupo entrenado");       
        // Identificar qué caras de las detectadas están en el grupo entrenado
        List<IdentifyResult> identifyResults = client.faces().identify(PERSONGROUP_ID, detectedFaces, null);        

        // Print each person group person (the person ID) identified from our results.
        System.out.println("    >> Personas identificadas en la foto de grupo: ");
        for (IdentifyResult result : identifyResults) {
            System.out.println("     - Person ID: " + result.faceId().toString());
            if (result.candidates().size()!=0) {
                Person matchedPerson = client.personGroupPersons().get(PERSONGROUP_ID, result.candidates().get(0).personId());
                System.out.println("      >> Matches with person: " + matchedPerson.name());
                System.out.println("      >> Matches with confidence: " + result.candidates().get(0).confidence());
            } else {
                System.out.println("      >> No matches");
            }
        }        
    }    

    
    /**
     * Detect Face
     * Detects the face(s) in an image URL.
     */
    private static List<UUID> detectFaces(FaceAPI client, String imageURL) {
        // Create face IDs list
        List<DetectedFace> facesList = client.faces().detectWithUrl(imageURL, new DetectWithUrlOptionalParameter().withReturnFaceId(true));
        System.out.println("    >> Detected face ID(s) from URL image: " + imageURL  + " :");
        // Get face(s) UUID(s)
        List<UUID> faceUuids = new ArrayList<>();
        for (DetectedFace face : facesList) {
            faceUuids.add(face.faceId());
            System.out.println("     - FaceID: " + face.faceId()); 
        }

        return faceUuids;
    }    
}
