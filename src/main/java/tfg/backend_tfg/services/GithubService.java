package tfg.backend_tfg.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tfg.backend_tfg.model.Equipo;
import tfg.backend_tfg.model.GitHubUserDetails;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.repository.EquipoRepository;
import tfg.backend_tfg.repository.UsuarioRepository;

@Service
public class GithubService {

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.app.id}")
    private String appId;

    @Value("${github.app.privateKeyPath}")
    private String privateKeyPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EquipoRepository equipoRepository;

    // 1. Leer la clave privada desde el archivo PEM y convertirla a RSAPrivateKey
    private RSAPrivateKey getPrivateKey() throws Exception {
        String privateKeyPEM = new String(java.nio.file.Files.readAllBytes(Paths.get(privateKeyPath)));

        privateKeyPEM = privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
    }

    // 2. Generar JWT para la GitHub App
    private String generateJWT() throws Exception {
        Instant now = Instant.now();
        RSAPrivateKey privateKey = getPrivateKey();

        Algorithm algorithm = Algorithm.RSA256(null, privateKey); // Clave privada para firmar
        return JWT.create()
                .withIssuedAt(now)
                .withExpiresAt(now.plusSeconds(600)) // 10 minutos de validez
                .withIssuer(appId)
                .sign(algorithm);
    }

    // 3. Obtener el token de instalación usando el installation_id
    private String getInstallationAccessToken(String installationId) throws Exception {
        String jwt = generateJWT();
        String url = String.format("https://api.github.com/app/installations/%s/access_tokens", installationId);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(url);
            request.addHeader("Authorization", "Bearer " + jwt);
            request.addHeader("Accept", "application/vnd.github+json");

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonNode json = objectMapper.readTree(responseBody);
                return json.get("token").asText();
            }
        }
    }

    // 4. obtener installation id
    private String getInstallationId(String organizacion) throws Exception {
        String jwt = generateJWT();
        String url = "https://api.github.com/app/installations";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.addHeader("Authorization", "Bearer " + jwt);
            request.addHeader("Accept", "application/vnd.github+json");

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonNode installations = objectMapper.readTree(responseBody);

                for (JsonNode installation : installations) {
                    if (installation.get("account").get("login").asText().equals(organizacion)) {
                        return installation.get("id").asText();
                    }
                }
            }
        }
        throw new RuntimeException("No se encontró el Installation ID para la organización: " + organizacion);
    }

    // 5. validar org
    public Map<String, Boolean> validarOrganizacion(Integer profesorId, List<Integer> miembrosIds, String organizacionUrl) throws Exception {
        String organizacion = organizacionUrl.replace("https://github.com/", "").replaceAll("/$", "");
        String installationId = getInstallationId(organizacion);
        String accessToken = getInstallationAccessToken(installationId);

        // Obtener git_username de los miembros
        List<String> miembrosGitUsernames = usuarioRepository.findAllById(miembrosIds)
                .stream()
                .map(Usuario::getGitUsername)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Obtener git_username del profesor
        Usuario profesor = usuarioRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado."));
        String profesorGitUsername = profesor.getGitUsername();

        if (profesorGitUsername == null) {
            throw new RuntimeException("El profesor no tiene una cuenta de GitHub configurada.");
        }

        // Validar si todos los miembros y el profesor pertenecen a la organización
        Map<String, Boolean> resultadoValidacion = new HashMap<>();
        resultadoValidacion.put("todosUsuariosGitConfigurados", miembrosGitUsernames.size() == miembrosIds.size());
        resultadoValidacion.put("profesorGitConfigurado", profesorGitUsername != null);

        String url = String.format("https://api.github.com/orgs/%s/members", organizacion);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.addHeader("Authorization", "Bearer " + accessToken);
            request.addHeader("Accept", "application/vnd.github+json");

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonNode members = objectMapper.readTree(responseBody);
                List<String> miembrosOrg = members.findValuesAsText("login");

                // Validar miembros
                boolean todosMiembrosValidos = miembrosOrg.containsAll(miembrosGitUsernames);
                boolean profesorValido = miembrosOrg.contains(profesorGitUsername);

                resultadoValidacion.put("todosMiembrosEnOrganizacion", todosMiembrosValidos);
                resultadoValidacion.put("profesorEnOrganizacion", profesorValido);
            }
        }

        return resultadoValidacion;
    }

    // 6. modificar bd
    public void asignarOrganizacion(Integer equipoId, String organizacionUrl) {
        Equipo equipo = equipoRepository.findById(equipoId)
            .orElseThrow(() -> new IllegalStateException("Equipo no encontrado"));

        equipo.setGitOrganizacion(organizacionUrl.replace("https://github.com/", "").replaceAll("/$", ""));
        equipoRepository.save(equipo);
    }

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
