package com.rovertrackerapi;

import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
public class RoverTrackerApiController {

	@RequestMapping("/")
	public String index() {
		return "Request successful.";
	}
	@CrossOrigin
	@RequestMapping("/MarsRoverTracker")
	public ResponseEntity<Object> marsRoverTracker(@RequestParam(value = "date", defaultValue = "2021-6-6") String date) {
		// Get raw data from NASA's API
		String rawData = NasaApiHandlers.marsRoverTrackerAsString(date);
		// Server error handling
		if(rawData.contains("error")) return new ResponseEntity<>(JsonParser.parseString(rawData).getAsJsonObject(), HttpStatus.OK);
		// Format raw data into JSON
		JsonObject roverObj = NasaApiHandlers.formatMarsRoverJson(rawData);
		// Send it
		return new ResponseEntity<>(roverObj, HttpStatus.OK);
  }
}