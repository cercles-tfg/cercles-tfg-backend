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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tfg.backend_tfg.model.GitHubUserDetails;

@Service
public class GithubService {

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    public Pair<String, String> obtenerNombreUsuarioGitHub(String code) {
        String accessTokenUrl = "https://github.com/login/oauth/access_token";
        String userApiUrl = "https://api.github.com/user";

        System.out.println("Client ID: " + clientId);
        System.out.println("Client Secret: " + clientSecret);
        System.out.println("Authorization Code: " + code);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Paso 1: Intercambiar el código por un access token
            HttpPost tokenRequest = new HttpPost(accessTokenUrl);
            tokenRequest.setHeader("Accept", "application/json");
            tokenRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("code", code));
            tokenRequest.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));

            try (CloseableHttpResponse response = client.execute(tokenRequest)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("Respuesta del token: " + responseBody);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode tokenJson = objectMapper.readTree(responseBody);

                JsonNode accessTokenNode = tokenJson.get("access_token");
                if (accessTokenNode == null || accessTokenNode.asText().isEmpty()) {
                    System.err.println("No se encontró el access_token en la respuesta");
                    return null;
                }

                String accessToken = accessTokenNode.asText();

                // Paso 2: Usar el access token para obtener el nombre de usuario de GitHub
                HttpGet userRequest = new HttpGet(userApiUrl);
                userRequest.setHeader("Authorization", "Bearer " + accessToken);
                userRequest.setHeader("Accept", "application/json");

                try (CloseableHttpResponse userResponse = client.execute(userRequest)) {
                    String userResponseBody = EntityUtils.toString(userResponse.getEntity());
                    System.out.println("Respuesta del usuario: " + userResponseBody);

                    JsonNode userJson = objectMapper.readTree(userResponseBody);
                    JsonNode loginNode = userJson.get("login");

                    if (loginNode == null || loginNode.asText().isEmpty()) {
                        System.err.println("No se encontró el nombre de usuario de GitHub en la respuesta");
                        return null;
                    }

                    String username = loginNode.asText();
                    return Pair.of(username, accessToken);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Método para obtener detalles adicionales del usuario GitHub
    public GitHubUserDetails obtenerDetallesAdicionalesUsuarioGitHub(String accessToken) {
        String userApiUrl = "https://api.github.com/user";
        String reposApiUrl = "https://api.github.com/user/repos";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ObjectMapper objectMapper = new ObjectMapper();

            // Paso 1: Obtener detalles básicos del usuario
            HttpGet userRequest = new HttpGet(userApiUrl);
            userRequest.setHeader("Authorization", "Bearer " + accessToken);
            userRequest.setHeader("Accept", "application/json");

            try (CloseableHttpResponse userResponse = client.execute(userRequest)) {
                String userResponseBody = EntityUtils.toString(userResponse.getEntity());

                // Parsear la respuesta para obtener información básica
                JsonNode userJson = objectMapper.readTree(userResponseBody);
                JsonNode loginNode = userJson.get("login");
                JsonNode publicReposNode = userJson.get("public_repos");
                JsonNode followersNode = userJson.get("followers");
                JsonNode followingNode = userJson.get("following");
                JsonNode companyNode = userJson.get("company");
                JsonNode locationNode = userJson.get("location");

                if (loginNode == null || loginNode.asText().isEmpty()) {
                    System.err.println("No se encontró el nombre de usuario de GitHub en la respuesta");
                    return null;
                }

                GitHubUserDetails userDetails = new GitHubUserDetails();
                userDetails.setLogin(loginNode.asText());
                userDetails.setPublicRepos(publicReposNode != null ? publicReposNode.asInt() : 0);
                userDetails.setFollowers(followersNode != null ? followersNode.asInt() : 0);
                userDetails.setFollowing(followingNode != null ? followingNode.asInt() : 0);
                userDetails.setCompany(companyNode != null ? companyNode.asText() : "No especificado");
                userDetails.setLocation(locationNode != null ? locationNode.asText() : "No especificada");

                // Paso 2: Obtener el número total de repositorios del usuario y detalles adicionales
                HttpGet reposRequest = new HttpGet(reposApiUrl);
                reposRequest.setHeader("Authorization", "Bearer " + accessToken);
                reposRequest.setHeader("Accept", "application/json");

                try (CloseableHttpResponse reposResponse = client.execute(reposRequest)) {
                    String reposResponseBody = EntityUtils.toString(reposResponse.getEntity());
                    JsonNode reposJson = objectMapper.readTree(reposResponseBody);

                    int privateReposCount = 0;
                    int totalCommits = 0;

                    for (JsonNode repo : reposJson) {
                        if (repo.get("private").asBoolean()) {
                            privateReposCount++;
                        }

                        // Para cada repositorio, obtenemos el número de commits (esta es una simplificación)
                        String commitsUrl = repo.get("commits_url").asText().replace("{/sha}", "");
                        HttpGet commitsRequest = new HttpGet(commitsUrl);
                        commitsRequest.setHeader("Authorization", "Bearer " + accessToken);
                        commitsRequest.setHeader("Accept", "application/json");

                        try (CloseableHttpResponse commitsResponse = client.execute(commitsRequest)) {
                            String commitsResponseBody = EntityUtils.toString(commitsResponse.getEntity());
                            JsonNode commitsJson = objectMapper.readTree(commitsResponseBody);
                            totalCommits += commitsJson.size();
                        }
                    }

                    userDetails.setPrivateRepos(privateReposCount);
                    userDetails.setTotalCommits(totalCommits);
                }

                return userDetails;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
