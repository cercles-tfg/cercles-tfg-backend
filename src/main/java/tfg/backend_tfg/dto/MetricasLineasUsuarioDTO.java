package tfg.backend_tfg.dto;

public class MetricasLineasUsuarioDTO {
    private String username;
    private int linesAdded;
    private int linesRemoved;

    // Constructor vacío
    public MetricasLineasUsuarioDTO() {}

    // Constructor con username
    public MetricasLineasUsuarioDTO(String username) {
        this.username = username;
    }

    // Métodos Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    // Método para combinar métricas de otro DTO
    public void combine(MetricasLineasUsuarioDTO other) {
        this.linesAdded += other.linesAdded;
        this.linesRemoved += other.linesRemoved;
    }

    @Override
    public String toString() {
        return "MetricasLineasUsuarioDTO{" +
                "username='" + username + '\'' +
                ", linesAdded=" + linesAdded +
                ", linesRemoved=" + linesRemoved +
                '}';
    }
}
