package com.rovertrackerapi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NasaApiHandlers {
  private static String apiKey="xifrHQYiC5Vyc9P2F3Fog7ih0lzMzAjiiaHbGFCo";

  public static String marsRoverTrackerAsString(String date) {
    String response = "{ \"error\": true, ";
    try{
      // Set URL
      URL url = new URL(String.format("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?"
      + "earth_date=" + date
      + "&api_key=" + apiKey));

      // Open connection
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");

      // Set request headers
      con.setRequestProperty("Content-Type", "application/json");

      // Set timeouts
      con.setConnectTimeout(5000);
      con.setReadTimeout(5000);

      // Handle redirects
      con.setInstanceFollowRedirects(false);

      // Handle response
      int status = con.getResponseCode();
      if(status < 400) {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while((inputLine = in.readLine()) != null) {
          content.append(inputLine);
        }
        in.close();
        response = content.toString();
      }
      else { response += "\"status\": " + status + ", \"location\": \"NASA API\" }"; }
      con.disconnect();
    } catch (Exception e) {
      System.out.println("*** Error creating request: " + e);
      response += "\"status\": 500, \"location\": \"My API\" }";
    }

    // Send it
    return response;
  }

  public static JsonObject formatMarsRoverJson (String rawData) {
    // The JSON returned from here will be in the following format:
		//	{
		//		rovers: [
		//			aRover: {
		//				...stuff,
		//				cameras: [
		//					aCamera: {
		//						...stuff,	
		//						photos: [
		//							...photos
		//						]					
		//					}
		//				]
		//			}
		//		]
		//  }
		// i.e. separated by rover, by camera on rover, by photo from camera

		JsonObject jobject = JsonParser.parseString(rawData).getAsJsonObject();
    JsonObject finalJObject = new JsonObject();
		JsonArray rawArray = jobject.getAsJsonArray("photos");
		if(rawArray.isEmpty()) {
			return JsonParser.parseString("{ \"error\": true, \"status\": 404, \"location\": \"NASA API\" }").getAsJsonObject();
		}
		JsonArray roverArray = new JsonArray();
		
		// For every element in the raw array
		for (int i=0; i<rawArray.size(); i++) {
			int foundAt = -1;
			JsonObject aPhoto = rawArray.get(i).getAsJsonObject();
			// For every element in the rover array
			for (int j=0; j<roverArray.size();j++) {
				// Check if the rover in the raw array element is in the rover array
				String rovArrayId = roverArray.get(j).getAsJsonObject().get("id").getAsString();
				String rovPhotoId = aPhoto.get("rover").getAsJsonObject().get("id").getAsString();
				if(rovArrayId.equals(rovPhotoId)) {
					foundAt = j;
				}
			}
			// If rover isn't found in rover array, then add it
			if(foundAt < 0) {
				roverArray.add(aPhoto.get("rover"));
				foundAt = roverArray.size()-1;
			}
			// In the rover element, check to see if cameras array exists. If not, then add it
			JsonObject theRoverObj = roverArray.get(foundAt).getAsJsonObject();
			JsonArray cameraArray;
			if(!theRoverObj.has("cameras")) {
				cameraArray = new JsonArray();
				theRoverObj.add("cameras", cameraArray);
			} else {
				cameraArray = theRoverObj.get("cameras").getAsJsonArray();
			}
			// For every element in the camera array
			foundAt = -1;
			for (int j=0; j<cameraArray.size();j++) {
				// Check if the camera in the raw array element is in the camera array of the rover element
				String camArrayId = cameraArray.get(j).getAsJsonObject().get("id").getAsString();
				String camPhotoId = aPhoto.get("camera").getAsJsonObject().get("id").getAsString();
				if(camArrayId.equals(camPhotoId)) {
					foundAt = j;
				}
			}
			// If camera isn't found in camera array, then add it
			if(foundAt < 0) {
				cameraArray.add(aPhoto.get("camera"));
				foundAt = roverArray.size()-1;
			}
			// In the camera element, check to see if photos array exists. If not, then add it
			JsonObject theCameraObj = cameraArray.get(foundAt).getAsJsonObject();
			JsonArray photoArray;
			if(!theCameraObj.has("photos")) {
				photoArray = new JsonArray();
				theCameraObj.add("photos", photoArray);
			} else {
				photoArray = theCameraObj.get("photos").getAsJsonArray();
			}
			// Add the photo to the photo array of the camera element
			photoArray.add(aPhoto.get("img_src"));
		}
    finalJObject.add("rovers", roverArray);
    return finalJObject;
  }
}
