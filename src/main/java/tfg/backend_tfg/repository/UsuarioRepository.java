package tfg.backend_tfg.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import tfg.backend_tfg.model.Rol;
import tfg.backend_tfg.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreo(String correo);

    List<Usuario> findAllByRol(Rol rol);


}
