package tfg.backend_tfg.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearEvaluacionDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer cursoId;
}