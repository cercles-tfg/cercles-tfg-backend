package tfg.backend_tfg.controller;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tfg.backend_tfg.dto.CrearEquipoDTO;
import tfg.backend_tfg.dto.EquipoDetalleDTO;
import tfg.backend_tfg.dto.EquipoSummaryDTO;
import tfg.backend_tfg.model.Equipo;
import tfg.backend_tfg.model.Estudiante;
import tfg.backend_tfg.repository.EstudianteRepository;
import tfg.backend_tfg.repository.UsuarioRepository;
import tfg.backend_tfg.services.EquipoService;

@RestController
@RequestMapping("/api/equipos")
public class EquipoController {
    
    @Autowired
    private EquipoService equipoService;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


    @GetMapping("/{equipoId}")
    public ResponseEntity<EquipoDetalleDTO> obtenerEquipoDetalle(@PathVariable int equipoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            // Devolver una lista vacía con un estado HTTP 403
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        try {
            EquipoDetalleDTO equipoDetalle = equipoService.obtenerEquipoDetallePorId(equipoId);
            return ResponseEntity.ok(equipoDetalle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @PostMapping("/crear")
    public ResponseEntity<String> crearEquipo(@RequestBody CrearEquipoDTO crearEquipoDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
        }

        String correo = authentication.getName();
        int estudianteId = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado o no es un estudiante."))
                .getId();

        try {
            // Validar que el estudiante pertenece al curso
            equipoService.validarEstudianteCurso(estudianteId, crearEquipoDTO.getCursoId());

            // Crear el equipo
            Equipo equipo = equipoService.crearEquipo(
                    crearEquipoDTO.getNombre(),
                    crearEquipoDTO.getCursoId(),
                    crearEquipoDTO.getEvaluadorId(),
                    crearEquipoDTO.getEstudiantesIds()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body("Equipo creado exitosamente. ID: " + equipo.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el equipo: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/borrar")
    public ResponseEntity<String> borrarEquipo(@PathVariable int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
        }
        try {
            equipoService.borrarEquipo(id);
            return ResponseEntity.ok("Equipo borrado exitosamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al borrar el equipo.");
        }
    }

    @PreAuthorize("hasAuthority('ESTUDIANTE')")
    @DeleteMapping("/{equipoId}/salir")
    public ResponseEntity<String> salirseDelEquipo(@PathVariable int equipoId, @RequestBody Map<String, Integer> body) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
        }
        try {
            int estudianteId = body.get("estudianteId");
            equipoService.salirseDelEquipo(equipoId, estudianteId);
            return ResponseEntity.ok("Estudiante salió del equipo correctamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al salir del equipo.");
        }
    }

    @PostMapping("/{id}/add_member")
    public ResponseEntity<String> añadirMiembros(@PathVariable int id, @RequestBody Map<String, List<Integer>> body) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
        }

        try {
            List<Integer> estudiantesIds = body.get("estudiantesIds");
            equipoService.añadirMiembros(id, estudiantesIds);
            return ResponseEntity.ok("Miembros añadidos correctamente al equipo.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al añadir los miembros al equipo.");
        }
    }

    @DeleteMapping("/{id}/borrar_miembros")
    public ResponseEntity<String> borrarMiembros(@PathVariable int id, @RequestBody Map<String, List<Integer>> body) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado.");
        }

        try {
            List<Integer> estudiantesIds = body.get("estudiantesIds");
            equipoService.borrarMiembros(id, estudiantesIds);
            return ResponseEntity.ok("Miembros eliminados correctamente del equipo.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar los miembros del equipo.");
        }
    }

}
