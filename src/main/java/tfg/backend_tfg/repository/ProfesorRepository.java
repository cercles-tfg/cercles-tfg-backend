package tfg.backend_tfg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tfg.backend_tfg.model.Profesor;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Integer> {
}

