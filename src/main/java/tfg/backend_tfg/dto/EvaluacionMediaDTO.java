package tfg.backend_tfg.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluacionMediaDTO {
    private Integer estudianteId;
    private List<MediaPorEvaluacionDTO> mediaPorEvaluacion;
    private double mediaGeneralDeCompañeros;
    private double mediaGeneralPropia;
}
