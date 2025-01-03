package tfg.backend_tfg.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TaigaService {

    private static final String TAIGA_API_BASE_URL = "https://api.taiga.io/api/v1";
    private final RestTemplate restTemplate;

    public TaigaService() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> authenticateTaigaUser(String username, String password) {
        String authToken = obtenerTokenNormal(username, password);
        return authToken != null ? obtenerUsuarioActual(authToken) : null;
    }

    private String obtenerTokenNormal(String username, String password) {
        try {
            String url = TAIGA_API_BASE_URL + "/auth";
            Map<String, String> body = Map.of(
                "type", "normal",
                "username", username,
                "password", password
            );
    
            ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("auth_token");
            } else {
                System.err.println("Error en autenticación normal: " + response.getStatusCode());
                System.err.println("Cuerpo de respuesta: " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public Map<String, Object> authenticateTaigaUserWithGitHub(String token) {
        String authToken = obtenerTokenGitHub(token);
        return authToken != null ? obtenerUsuarioActual(authToken) : null;
    }

    private String obtenerTokenGitHub(String token) {
        try {
            String url = TAIGA_API_BASE_URL + "/auth";
            Map<String, String> body = Map.of(
                "type", "github",
                "code", token
            );
    
            ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("auth_token");
            } else {
                System.err.println("Error en autenticación con GitHub: " + response.getStatusCode());
                System.err.println("Cuerpo de respuesta: " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, Object> obtenerUsuarioActual(String authToken) {
        try {
            String url = TAIGA_API_BASE_URL + "/users/me";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
