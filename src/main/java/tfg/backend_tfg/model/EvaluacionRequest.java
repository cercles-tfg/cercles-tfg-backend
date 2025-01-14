package tfg.backend_tfg.model;

import java.time.LocalDate;

public class EvaluacionRequest {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Getters y setters
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
}

