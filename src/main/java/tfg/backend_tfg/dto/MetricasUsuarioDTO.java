package tfg.backend_tfg.dto;


public class MetricasUsuarioDTO {
    private String nombre;
    private String username;
    private int totalCommits;
    private int pullRequestsCreated;
    private int pullRequestsMerged;

    // Issues específicos por usuario
    private int userStories;
    private int userStoriesClosed;
    private int tasks;
    private int tasksClosed;

    // Constructor vacío
    public MetricasUsuarioDTO() {}

    // Constructor con username
    public MetricasUsuarioDTO(String username) {
        this.username = username;
    }

    // Constructor con nombre y username
    public MetricasUsuarioDTO(String nombre, String username) {
        this.nombre = nombre;
        this.username = username;
    }

    // Método para combinar métricas de otro DTO
    public void combine(MetricasUsuarioDTO other) {
        this.totalCommits += other.totalCommits;
        this.pullRequestsCreated += other.pullRequestsCreated;
        this.pullRequestsMerged += other.pullRequestsMerged;
        this.userStories += other.userStories;
        this.userStoriesClosed += other.userStoriesClosed;
        this.tasks += other.tasks;
        this.tasksClosed += other.tasksClosed;
    }

    @Override
    public String toString() {
        return "MetricasUsuarioDTO{" +
                "username='" + username + '\'' +
                ", totalCommits=" + totalCommits +
                ", pullRequestsCreated=" + pullRequestsCreated +
                ", pullRequestsMerged=" + pullRequestsMerged +
                ", userStories=" + userStories +
                ", userStoriesClosed=" + userStoriesClosed +
                ", tasks=" + tasks +
                ", tasksClosed=" + tasksClosed +
                '}';
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTotalCommits() {
        return totalCommits;
    }

    public void setTotalCommits(int totalCommits) {
        this.totalCommits = totalCommits;
    }


    public int getPullRequestsCreated() {
        return pullRequestsCreated;
    }

    public void setPullRequestsCreated(int pullRequestsCreated) {
        this.pullRequestsCreated = pullRequestsCreated;
    }

    public int getPullRequestsMerged() {
        return pullRequestsMerged;
    }

    public void setPullRequestsMerged(int pullRequestsMerged) {
        this.pullRequestsMerged = pullRequestsMerged;
    }

    public int getUserStories() {
        return userStories;
    }

    public void setUserStories(int userStories) {
        this.userStories = userStories;
    }

    public int getUserStoriesClosed() {
        return userStoriesClosed;
    }

    public void setUserStoriesClosed(int userStoriesClosed) {
        this.userStoriesClosed = userStoriesClosed;
    }

    public int getTasks() {
        return tasks;
    }

    public void setTasks(int tasks) {
        this.tasks = tasks;
    }

    public int getTasksClosed() {
        return tasksClosed;
    }

    public void setTasksClosed(int tasksClosed) {
        this.tasksClosed = tasksClosed;
    }
}
