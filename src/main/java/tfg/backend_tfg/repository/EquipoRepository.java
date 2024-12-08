package tfg.backend_tfg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tfg.backend_tfg.model.Equipo;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Integer> {
    boolean existsByNombreAndCursoId(String nombre, int cursoId);
}

