package tfg.backend_tfg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaPorEvaluacionDTO {
    private Integer evaluacionId;
    private double mediaDeCompañeros;
    private int notaPropia;
}
