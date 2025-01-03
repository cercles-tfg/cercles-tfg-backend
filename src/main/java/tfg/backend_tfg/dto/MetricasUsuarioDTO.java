package tfg.backend_tfg.dto;

import java.util.ArrayList;
import java.util.List;

public class MetricasUsuarioDTO {
    private String nombre;
    private String username;
    private int totalCommits;
    private int linesAdded;
    private int linesRemoved;
    private int pullRequestsCreated;

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
        this.linesAdded += other.linesAdded;
        this.linesRemoved += other.linesRemoved;
        this.pullRequestsCreated += other.pullRequestsCreated;
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
                ", linesAdded=" + linesAdded +
                ", linesRemoved=" + linesRemoved +
                ", pullRequestsCreated=" + pullRequestsCreated +
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

    public int getLinesAdded() {
        return linesAdded;
    }

    public void setLinesAdded(int linesAdded) {
        this.linesAdded = linesAdded;
    }

    public int getLinesRemoved() {
        return linesRemoved;
    }

    public void setLinesRemoved(int linesRemoved) {
        this.linesRemoved = linesRemoved;
    }

    public int getPullRequestsCreated() {
        return pullRequestsCreated;
    }

    public void setPullRequestsCreated(int pullRequestsCreated) {
        this.pullRequestsCreated = pullRequestsCreated;
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
