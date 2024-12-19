package tfg.backend_tfg.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionDetalleRequestDTO {
    private Integer evaluacionId;
    private Integer evaluadorId;
    private List<EvaluacionDetalleDTO> detalles;
}

