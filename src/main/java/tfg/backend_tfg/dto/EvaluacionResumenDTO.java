package tfg.backend_tfg.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionResumenDTO {
    private Integer evaluadoId;
    private List<EvaluacionPorEvaluacionIdDTO> evaluaciones;
    
}
