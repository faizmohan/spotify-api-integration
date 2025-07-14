package com.faiz.spotify.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.faiz.spotify.models.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class MusicController {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${spotify.client.id}")
	private String clientId;

	@Value("${spotify.client.secret}")
	private String clientSecret;

	@Value("${redirect.server.ip}")
	private String redirectUri;

	@GetMapping("/login")
	public ResponseEntity<Void> login() {
		String scopes = "user-top-read user-read-currently-playing user-modify-playback-state user-read-private";
		String redirectUrl = "https://accounts.spotify.com/authorize?" + "client_id=" + clientId + "&response_type=code"
				+ "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) + "&scope="
				+ URLEncoder.encode(scopes, StandardCharsets.UTF_8);

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(java.net.URI.create(redirectUrl));
		return new ResponseEntity<>(headers, HttpStatus.FOUND);
	}

	@GetMapping("/spotify")
	public Object getUserTopTracks(@RequestParam String code) {

		try {

			User user = new User();

			String loginURL = "https://accounts.spotify.com/api/token";

			HttpHeaders headers = new HttpHeaders();

			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			String authString = clientId + ":" + clientSecret;
			String base64Auth = Base64.getEncoder().encodeToString(authString.getBytes());
			headers.set("Authorization", "Basic " + base64Auth);

			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("grant_type", "authorization_code");
			form.add("code", code);
			form.add("redirect_uri", redirectUri);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

			ResponseEntity<Map> response = restTemplate.postForEntity(loginURL, request, Map.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				Map<String, Object> tokenData = response.getBody();
				String accessToken = (String) tokenData.get("access_token");

				HttpHeaders apiHeaders = new HttpHeaders();
				apiHeaders.setBearerAuth(accessToken);
				HttpEntity<String> apiRequest = new HttpEntity<>(apiHeaders);

				String apiUrl = "https://api.spotify.com/v1/me/top/tracks?limit=10";
				ResponseEntity<String> apiResponse = restTemplate.exchange(apiUrl, HttpMethod.GET, apiRequest,
						String.class);

				JsonNode root = objectMapper.readTree(apiResponse.getBody());
				JsonNode items = root.path("items");

				List<String> trackNames = new ArrayList<>();
				List<String> uris = new ArrayList<>();
				if (items.isArray()) {
					for (JsonNode item : items) {
						String name = item.path("name").asText();
						String uri = item.path("uri").asText();
						trackNames.add(name);
						uris.add(uri);

					}
				}
				user.setTrackNames(trackNames);
				user.setTrackURIs(uris);
				apiUrl = "https://api.spotify.com/v1/me/player/currently-playing";
				apiResponse = restTemplate.exchange(apiUrl, HttpMethod.GET, apiRequest, String.class);

				if (apiResponse.getBody() != null && !apiResponse.getBody().isEmpty()) {
					root = objectMapper.readTree(apiResponse.getBody());
					items = root.path("item");

					String name = items.path("name").asText();
					user.setCurrent_playing_track(name);
				} else {
					user.setCurrent_playing_track("");
				}

				apiUrl = "https://api.spotify.com/v1/me/player/pause";
				try {
					apiResponse = restTemplate.exchange(apiUrl, HttpMethod.GET, apiRequest, String.class);
					if (apiResponse.getStatusCode().is2xxSuccessful()) {

						user.setPause(apiResponse.getBody());
					} else {
						root = objectMapper.readTree(apiResponse.getBody());
						items = root.path("error");

						String msg = items.path("message").asText();
						user.setPause(msg);

					}
				} catch (HttpClientErrorException ex) {
					String responseBody = ex.getResponseBodyAsString();
					root = objectMapper.readTree(responseBody);
					String msg = root.path("error").path("message").asText();
					user.setPause("Error: " + msg);

				} catch (HttpServerErrorException ex) {
					user.setPause("Spotify server error: " + ex.getStatusCode());

				} catch (Exception ex) {
					user.setPause("Unexpected error: " + ex.getMessage());
				}

				apiUrl = "https://api.spotify.com/v1/me";
				apiResponse = restTemplate.exchange(apiUrl, HttpMethod.GET, apiRequest, String.class);

				if (apiResponse.getStatusCode().is2xxSuccessful()) {

					root = objectMapper.readTree(apiResponse.getBody());
					items = root.path("product");

					user.setProductType(items.asText());
				} else {
					root = objectMapper.readTree(apiResponse.getBody());
					items = root.path("error");

					String msg = items.path("message").asText();
					user.setProductType(msg);

				}

				if (user.getProductType() != null && !user.getProductType().isEmpty()
						&& user.getProductType().equalsIgnoreCase("premium")) {
					apiUrl = "https://api.spotify.com/v1/me/player/play";

					apiHeaders.setContentType(MediaType.APPLICATION_JSON);

					Map<String, Object> body = new HashMap<>();
					body.put("uris", uris);
					body.put("offset", Map.of("position", 0));

					HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, apiHeaders);
					try {
						apiResponse = restTemplate.exchange(apiUrl, HttpMethod.PUT, entity, String.class);

						if (apiResponse.getStatusCode().is2xxSuccessful()) {

							user.setPlay(apiResponse.getBody());
						} else {
							root = objectMapper.readTree(apiResponse.getBody());
							items = root.path("error");

							String msg = items.path("message").asText();
							user.setPlay(msg);

						}
					} catch (HttpClientErrorException ex) {
						String responseBody = ex.getResponseBodyAsString();
						root = objectMapper.readTree(responseBody);
						String msg = root.path("error").path("message").asText();
						user.setPlay("Error: " + msg);

					} catch (HttpServerErrorException ex) {
						user.setPlay("Spotify server error: " + ex.getStatusCode());

					} catch (Exception ex) {
						user.setPlay("Unexpected error: " + ex.getMessage());
					}
				} else {
					user.setPlay("");
				}

				return user;

			}

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to retrieve access token.");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String stackTrace = sw.toString();
			System.out.println(stackTrace);
		}
		return null;

	}
}
