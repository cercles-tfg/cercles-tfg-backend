package tfg.backend_tfg.services;

import java.io.IOException;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tfg.backend_tfg.dto.MetricasUsuarioDTO;
import tfg.backend_tfg.model.Equipo;
import tfg.backend_tfg.model.Estudiante;
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

    @Value("${professorat-amep.token}")
    private String profAmepToken;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private EstudianteRepository estudianteRepository;


    //1-8 funciones datos de una org

    //1. validar la org de un equipo
    public Map<String, Boolean> validarOrganizacion(Integer profesorId, List<Integer> miembrosIds, String organizacionUrl) {
        String organizacion = organizacionUrl.replace("https://github.com/", "").replaceAll("/$", "");

        // Obtener git_username de los miembros
        List<String> miembrosGitUsernames = usuarioRepository.findAllById(miembrosIds)
                .stream()
                .map(Usuario::getGitUsername)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Usuario profesor = usuarioRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado."));
        String profesorGitUsername = profesor.getGitUsername();

        Map<String, Boolean> resultadoValidacion = new HashMap<>();
        resultadoValidacion.put("todosUsuariosGitConfigurados", miembrosGitUsernames.size() == miembrosIds.size());
        resultadoValidacion.put("profesorGitConfigurado", profesorGitUsername != null);

        try {
            // Verificar si 'professorat-amep' es miembro
            boolean professoratMiembro = verificarMiembro(organizacion, "professorat-amep");
            resultadoValidacion.put("professoratEsMiembro", professoratMiembro);

            // Si 'professorat-amep' no es miembro, devolver todos los valores como false
            if (!professoratMiembro) {
                resultadoValidacion.put("professoratEsAdmin", false);
                resultadoValidacion.put("todosMiembrosEnOrganizacion", false);
                resultadoValidacion.put("profesorEnOrganizacion", false);
                return resultadoValidacion;
            }

            // Verificar si 'professorat-amep' es admin
            boolean professoratOwner = verificarOwner(organizacion, "professorat-amep");
            resultadoValidacion.put("professoratEsAdmin", professoratOwner);

            if (!professoratOwner) {
                resultadoValidacion.put("todosMiembrosEnOrganizacion", false);
                resultadoValidacion.put("profesorEnOrganizacion", false);
                return resultadoValidacion;
            }

            // Obtener miembros de la organización
            String url = String.format("https://api.github.com/orgs/%s/members", organizacion);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + profAmepToken);
            headers.set("Accept", "application/vnd.github+json");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

            List<String> miembrosOrg = response.getBody().findValuesAsText("login");

            // Validar miembros y profesor
            boolean todosMiembrosValidos = miembrosOrg.containsAll(miembrosGitUsernames);
            boolean profesorValido = miembrosOrg.contains(profesorGitUsername);

            resultadoValidacion.put("todosMiembrosEnOrganizacion", todosMiembrosValidos);
            resultadoValidacion.put("profesorEnOrganizacion", profesorValido);

        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error al comunicarse con la API de GitHub: " + e.getResponseBodyAsString());
        }

        return resultadoValidacion;
    }

    //2 verificar si professorat-amep es miembro
    private boolean verificarMiembro(String organizacion, String username) {
        try {
            String url = String.format("https://api.github.com/orgs/%s/members/%s", organizacion, username);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + profAmepToken);
            headers.set("Accept", "application/vnd.github+json");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

            return true; // Si no lanza excepción, es miembro
        } catch (HttpClientErrorException.NotFound e) {
            return false; // No es miembro
        }
    }

    //3 verificar que professorat-amep es admin/owner
    private boolean verificarOwner(String organizacion, String username) {
        try {
            String url = String.format("https://api.github.com/orgs/%s/memberships/%s", organizacion, username);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + profAmepToken);
            headers.set("Accept", "application/vnd.github+json");

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

            return "admin".equals(response.getBody().path("role").asText());
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Error al verificar owner: " + e.getResponseBodyAsString());
        }
    }

    // 4. modificar bd si org está bien
    public void asignarOrganizacion(Integer equipoId, String organizacionUrl) {
        Equipo equipo = equipoRepository.findById(equipoId)
            .orElseThrow(() -> new IllegalStateException("Equipo no encontrado"));

        equipo.setGitOrganizacion(organizacionUrl.replace("https://github.com/", "").replaceAll("/$", ""));
        equipoRepository.save(equipo);
    }

    // 5. Obtener repositorios de la organización
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

    //6. obtener metricas de un repo
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
    
    //7. comprobar si algun repo está vacio
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
    
    //8. obtener metricas de una org
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

    
    //9-13 funciones datos usuario

    // 9. obtener el nombre de usuario de github
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

    //10. conectar usuario con su github
    public Map<String, String> handleGitHubCallback(String email, String code) throws Exception {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(email);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        Pair<String, String> githubData = obtenerNombreUsuarioGitHub(code);

        if (githubData == null) {
            throw new IllegalStateException("Error al obtener datos de GitHub");
        }

        usuario.setGitUsername(githubData.getFirst());
        usuario.setGithubAccessToken(githubData.getSecond());
        usuarioRepository.save(usuario);

        return Map.of("message", "Cuenta de GitHub asociada exitosamente", "githubUsername", githubData.getFirst());
    }

    //11. buscar datos de github de un usuario
    public Map<String, Object> obtenerDatosUsuarioGitHub(String email) throws Exception {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(email);
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getGithubAccessToken() == null) {
            return null;
        }

        String accessToken = usuarioOpt.get().getGithubAccessToken();

        String reposUrl = "https://api.github.com/user/repos";
        String orgsUrl = "https://api.github.com/user/orgs";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        List<Map<String, Object>> repos = fetchGitHubData(reposUrl, entity);
        List<Map<String, Object>> orgs = fetchGitHubData(orgsUrl, entity);

        return Map.of(
            "repositorios", repos,
            "organizaciones", orgs
        );
    }

    //12. fetch datos de github
    private List<Map<String, Object>> fetchGitHubData(String url, HttpEntity<?> entity) {
        try {
            ResponseEntity<List> response = new RestTemplate().exchange(url, HttpMethod.GET, entity, List.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    //13. desconectar github de usuario 
    public void desconectarGitHub(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setGitUsername(null);
            usuario.setGithubAccessToken(null);
            usuarioRepository.save(usuario);
        }
    }

    
}
