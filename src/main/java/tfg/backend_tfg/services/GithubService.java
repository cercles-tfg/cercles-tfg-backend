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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tfg.backend_tfg.dto.MetricasUsuarioDTO;
import tfg.backend_tfg.model.Equipo;
import tfg.backend_tfg.model.Estudiante;
import tfg.backend_tfg.model.GitHubUserDetails;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.repository.EquipoRepository;
import tfg.backend_tfg.repository.EstudianteRepository;
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
    private RestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private EstudianteRepository estudianteRepository;

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
    private String getInstallationId(String organizacion, String jwt) throws Exception {
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
        String jwt = generateJWT();
        String installationId = getInstallationId(organizacion, jwt);
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

    // 7. obtener el nombre de usuario de github
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

    public String obtenerAccessTokenOrganizacion(String organizacion) throws Exception {
        // 1. Generar un JWT
        String jwt = generateJWT();

        // 2. Obtener la instalación asociada a la organización
        String installationId = getInstallationId(organizacion, jwt);

        // 3. Solicitar el token de acceso
        String tokenUrl = "https://api.github.com/app/installations/" + installationId + "/access_tokens";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);

        // Extraer el token del cuerpo de la respuesta
        return (String) response.getBody().get("token");
    }

    
    // Obtener repositorios de la organización
    public List<String> obtenerRepositorios(String organizacion, String accessToken) {
        String url = "https://api.github.com/orgs/" + organizacion + "/repos";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        List<String> repositorios = new ArrayList<>();
        response.getBody().forEach(repo -> repositorios.add(repo.get("name").asText()));
        return repositorios;
    }

    //obtener metricas de un repo
    public List<MetricasUsuarioDTO> obtenerMetricasRepositorio(String organizacion, String repo, List<String> usuarios, String accessToken) {
        if (isRepositorioVacio(organizacion, repo, accessToken)) {
            System.out.println("El repositorio " + repo + " está vacío. Se omite.");
            return Collections.emptyList();
        }
    
        String commitsUrl = "https://api.github.com/repos/" + organizacion + "/" + repo + "/commits";
        String pullsUrl = "https://api.github.com/repos/" + organizacion + "/" + repo + "/pulls";
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github+json");
        HttpEntity<?> entity = new HttpEntity<>(headers);
    
        Map<String, MetricasUsuarioDTO> metricsMap = new HashMap<>();
    
        for (String usuario : usuarios) {
            MetricasUsuarioDTO metrics = new MetricasUsuarioDTO();
            metrics.setUsername(usuario);
            metricsMap.put(usuario, metrics);
        }
    
        try {
            // Obtener commits y estadísticas
            ResponseEntity<JsonNode> commitsResponse = restTemplate.exchange(commitsUrl, HttpMethod.GET, entity, JsonNode.class);
            for (JsonNode commit : commitsResponse.getBody()) {
                String author = commit.path("author").path("login").asText();
                if (metricsMap.containsKey(author)) {
                    MetricasUsuarioDTO metrics = metricsMap.get(author);
                    metrics.setTotalCommits(metrics.getTotalCommits() + 1);
    
                    // Obtener detalles del commit
                    String commitUrl = commit.path("url").asText();
                    ResponseEntity<JsonNode> commitDetails = restTemplate.exchange(commitUrl, HttpMethod.GET, entity, JsonNode.class);
                    JsonNode stats = commitDetails.getBody().path("stats");
    
                    metrics.setLinesAdded(metrics.getLinesAdded() + stats.path("additions").asInt());
                    metrics.setLinesRemoved(metrics.getLinesRemoved() + stats.path("deletions").asInt());
                }
            }
    
            // Obtener Pull Requests
            ResponseEntity<JsonNode> pullsResponse = restTemplate.exchange(pullsUrl, HttpMethod.GET, entity, JsonNode.class);
            for (JsonNode pull : pullsResponse.getBody()) {
                String author = pull.path("user").path("login").asText();
                if (metricsMap.containsKey(author)) {
                    MetricasUsuarioDTO metrics = metricsMap.get(author);
                    metrics.setPullRequestsCreated(metrics.getPullRequestsCreated() + 1);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener métricas del repositorio " + repo + ": " + e.getMessage());
        }
    
        return new ArrayList<>(metricsMap.values());
    }
    

    //comprobar si algun repo está vacio
    public boolean isRepositorioVacio(String organizacion, String repo, String accessToken) {
        String repoUrl = "https://api.github.com/repos/" + organizacion + "/" + repo;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github+json");
    
        HttpEntity<?> entity = new HttpEntity<>(headers);
    
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(repoUrl, HttpMethod.GET, entity, JsonNode.class);
            JsonNode body = response.getBody();
            return body == null || body.path("default_branch").isMissingNode();
        } catch (Exception e) {
            System.err.println("Error al verificar si el repositorio está vacío: " + e.getMessage());
            return true; // Considera el repositorio como vacío si hay algún error
        }
    }
    
    //obtener metricas de una org
    public List<MetricasUsuarioDTO> obtenerMetricasOrganizacion(String organizacion, List<String> usuarios, String accessToken, List<Integer> estudiantesIds) {
        List<String> repositorios = obtenerRepositorios(organizacion, accessToken);
        Map<String, MetricasUsuarioDTO> aggregatedMetrics = new HashMap<>();

        // Asociar nombres a los usuarios usando los IDs de estudiantes
        Map<String, String> usernameToNombre = estudianteRepository.findAllById(estudiantesIds)
                .stream()
                .collect(Collectors.toMap(Estudiante::getGitUsername, Estudiante::getNombre));

        // Inicializar métricas agrupadas con nombres de usuario
        for (String usuario : usuarios) {
            String nombre = usernameToNombre.getOrDefault(usuario, "Desconocido");

            MetricasUsuarioDTO metrics = new MetricasUsuarioDTO();
            metrics.setUsername(usuario);
            metrics.setNombre(nombre);
            aggregatedMetrics.put(usuario, metrics);
        }

        // Iterar sobre repositorios
        for (String repo : repositorios) {
            if (isRepositorioVacio(organizacion, repo, accessToken)) {
                System.out.println("Repositorio vacío omitido: " + repo);
                continue;
            }

            List<MetricasUsuarioDTO> repoMetrics = obtenerMetricasRepositorio(organizacion, repo, usuarios, accessToken);
            for (MetricasUsuarioDTO repoMetric : repoMetrics) {
                MetricasUsuarioDTO aggregatedMetric = aggregatedMetrics.get(repoMetric.getUsername());
                aggregatedMetric.setTotalCommits(aggregatedMetric.getTotalCommits() + repoMetric.getTotalCommits());
                aggregatedMetric.setLinesAdded(aggregatedMetric.getLinesAdded() + repoMetric.getLinesAdded());
                aggregatedMetric.setLinesRemoved(aggregatedMetric.getLinesRemoved() + repoMetric.getLinesRemoved());
                aggregatedMetric.setPullRequestsCreated(aggregatedMetric.getPullRequestsCreated() + repoMetric.getPullRequestsCreated());
            }
        }

        return new ArrayList<>(aggregatedMetrics.values());
    }

    
    

    
}
