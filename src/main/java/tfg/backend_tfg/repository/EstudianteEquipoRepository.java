package tfg.backend_tfg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tfg.backend_tfg.model.EstudianteEquipo;
import tfg.backend_tfg.model.EstudianteEquipoId;

@Repository
public interface EstudianteEquipoRepository extends JpaRepository<EstudianteEquipo, EstudianteEquipoId> {
}
