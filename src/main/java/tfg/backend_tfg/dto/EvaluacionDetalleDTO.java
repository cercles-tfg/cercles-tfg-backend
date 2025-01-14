package tfg.backend_tfg.dto;

import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionDetalleDTO {
    private Integer id;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer cursoId;

    private Integer evaluacionId;
    private Integer evaluadorId;
    private Integer evaluadoId;
    private Integer puntos;
    private Integer equipoId;
}