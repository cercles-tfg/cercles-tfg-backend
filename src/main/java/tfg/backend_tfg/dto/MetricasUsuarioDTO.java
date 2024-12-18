package tfg.backend_tfg.dto;

public class MetricasUsuarioDTO {
    private String nombre;
    private String username;
    private int totalCommits;
    private int linesAdded;
    private int linesRemoved;
    private int pullRequestsCreated;

    @Override
    public String toString() {
        return "MetricasUsuarioDTO{" +
                "username='" + username + '\'' +
                ", totalCommits=" + totalCommits +
                ", linesAdded=" + linesAdded +
                ", linesRemoved=" + linesRemoved +
                ", pullRequestsCreated=" + pullRequestsCreated +
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

}
