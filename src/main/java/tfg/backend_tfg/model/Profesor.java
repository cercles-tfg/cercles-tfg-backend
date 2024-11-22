package tfg.backend_tfg.model;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Entity
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profesor extends Usuario {
    // Similar para la clase Profesor.
}