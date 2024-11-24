package tfg.backend_tfg.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class TaigaService {

    @Value("${taiga.client.id}")
    private String clientId;

    @Value("${taiga.client.secret}")
    private String clientSecret;

    private static final String TAIGA_AUTH_URL = "https://api.taiga.io/api/v1/auth";
    private static final String TAIGA_AUTH_GITHUB_URL = "https://api.taiga.io/api/v1/auth/github";

    public String authenticateTaigaUser(String username, String password) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Crear una solicitud POST para la autenticación
            HttpPost authRequest = new HttpPost(TAIGA_AUTH_URL);
            authRequest.setHeader("Content-Type", "application/json");

            // Crear el cuerpo de la solicitud con el nombre de usuario y la contraseña
            String jsonBody = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
            authRequest.setEntity(new StringEntity(jsonBody));

            // Ejecutar la solicitud
            try (CloseableHttpResponse response = client.execute(authRequest)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.err.println("Error al autenticar en Taiga. Código de estado: " + response.getStatusLine().getStatusCode());
                    return null;
                }

                // Parsear la respuesta para obtener el auth_token
                String responseBody = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode authTokenNode = jsonNode.get("auth_token");

                if (authTokenNode == null || authTokenNode.asText().isEmpty()) {
                    System.err.println("No se encontró el auth_token en la respuesta de Taiga");
                    return null;
                }

                return authTokenNode.asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String authenticateTaigaUserWithGitHub(String githubAccessToken) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Crear una solicitud POST para la autenticación a través de GitHub
            HttpPost authRequest = new HttpPost(TAIGA_AUTH_GITHUB_URL);
            authRequest.setHeader("Content-Type", "application/json");

            // Crear el cuerpo de la solicitud con el token de GitHub
            String jsonBody = String.format("{\"code\": \"%s\"}", githubAccessToken);
            authRequest.setEntity(new StringEntity(jsonBody));

            // Ejecutar la solicitud
            try (CloseableHttpResponse response = client.execute(authRequest)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.err.println("Error al autenticar en Taiga a través de GitHub. Código de estado: " + response.getStatusLine().getStatusCode());
                    return null;
                }

                // Parsear la respuesta para obtener el auth_token
                String responseBody = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode authTokenNode = jsonNode.get("auth_token");

                if (authTokenNode == null || authTokenNode.asText().isEmpty()) {
                    System.err.println("No se encontró el auth_token en la respuesta de Taiga");
                    return null;
                }

                return authTokenNode.asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Pair<String, String> obtenerNombreUsuarioTaiga(String code) {
        String tokenUrl = "https://api.taiga.io/auth";  // Aquí deberías poner la URL de Taiga para obtener el token
        String userApiUrl = "https://api.taiga.io/v1/users/me"; // URL para obtener los datos del usuario

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Paso 1: Intercambiar el código por un access token
            HttpPost tokenRequest = new HttpPost(tokenUrl);
            tokenRequest.setHeader("Accept", "application/json");
            tokenRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("code", code));
            tokenRequest.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));

            try (CloseableHttpResponse response = client.execute(tokenRequest)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode tokenJson = objectMapper.readTree(responseBody);

                JsonNode accessTokenNode = tokenJson.get("access_token");
                if (accessTokenNode == null || accessTokenNode.asText().isEmpty()) {
                    return null;
                }

                String accessToken = accessTokenNode.asText();

                // Paso 2: Usar el access token para obtener el nombre de usuario de Taiga
                HttpGet userRequest = new HttpGet(userApiUrl);
                userRequest.setHeader("Authorization", "Bearer " + accessToken);
                userRequest.setHeader("Accept", "application/json");

                try (CloseableHttpResponse userResponse = client.execute(userRequest)) {
                    String userResponseBody = EntityUtils.toString(userResponse.getEntity());
                    JsonNode userJson = objectMapper.readTree(userResponseBody);
                    JsonNode usernameNode = userJson.get("username");

                    if (usernameNode == null || usernameNode.asText().isEmpty()) {
                        return null;
                    }

                    String username = usernameNode.asText();
                    return Pair.of(username, accessToken);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
