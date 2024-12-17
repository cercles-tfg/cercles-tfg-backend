package tfg.backend_tfg.services;

import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TaigaService {

    private static final String TAIGA_AUTH_URL = "https://api.taiga.io/api/v1/auth";

    public String authenticateTaigaUser(String username, String password) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost authRequest = new HttpPost(TAIGA_AUTH_URL);
            authRequest.setHeader("Content-Type", "application/json");

            String jsonBody = String.format(
                "{\"type\": \"normal\", \"username\": \"%s\", \"password\": \"%s\"}",
                username,
                password
            );
            authRequest.setEntity(new StringEntity(jsonBody));

            try (CloseableHttpResponse response = client.execute(authRequest)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.err.println("Error al autenticar en Taiga. Código de estado: " + response.getStatusLine().getStatusCode());
                    return null;
                }

                String responseBody = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                return jsonNode.get("auth_token").asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String authenticateTaigaUserWithGitHub(String code) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost authRequest = new HttpPost(TAIGA_AUTH_URL);
            authRequest.setHeader("Content-Type", "application/json");

            String jsonBody = String.format(
                "{\"type\": \"github\", \"code\": \"%s\"}",
                code
            );
            authRequest.setEntity(new StringEntity(jsonBody));

            try (CloseableHttpResponse response = client.execute(authRequest)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.err.println("Error al autenticar en Taiga con GitHub. Código de estado: " + response.getStatusLine().getStatusCode());
                    return null;
                }

                String responseBody = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                return jsonNode.get("auth_token").asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
