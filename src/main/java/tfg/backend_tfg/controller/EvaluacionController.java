package tfg.backend_tfg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import tfg.backend_tfg.dto.CrearEvaluacionDTO;
import tfg.backend_tfg.dto.EvaluacionDetalleDTO;
import tfg.backend_tfg.dto.EvaluacionDetalleRequestDTO;
import tfg.backend_tfg.dto.EvaluacionMediaDTO;
import tfg.backend_tfg.dto.EvaluacionResumenDTO;
import tfg.backend_tfg.model.Evaluacion;
import tfg.backend_tfg.model.EvaluacionDetalle;
import tfg.backend_tfg.repository.EvaluacionDetalleRepository;
import tfg.backend_tfg.services.EvaluacionService;

import java.util.List;

@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionController {

    @Autowired
    private EvaluacionService evaluacionService;
    @Autowired
    private EvaluacionDetalleRepository evaluacionDetalleRepository;

    @PreAuthorize("hasAuthority('PROFESOR')")
    @PostMapping("/crear")
    public ResponseEntity<Evaluacion> crearEvaluacion(@RequestBody CrearEvaluacionDTO crearEvaluacionDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        return ResponseEntity.ok(evaluacionService.crearEvaluacion(crearEvaluacionDTO));
    }

    @PreAuthorize("hasAuthority('PROFESOR')")
    @GetMapping("/equipo/{equipoId}")
    public ResponseEntity<List<EvaluacionResumenDTO>> obtenerEvaluacionesPorEquipo(
            @PathVariable Integer equipoId,
            @RequestParam(required = false) List<Integer> evaluacionIds) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<EvaluacionResumenDTO> evaluaciones = evaluacionService.obtenerEvaluacionesPorEquipo(equipoId, evaluacionIds);
        return ResponseEntity.ok(evaluaciones);
    }


    @PreAuthorize("hasAuthority('PROFESOR')")
    @GetMapping("/equipo/{equipoId}/medias")
    public ResponseEntity<List<EvaluacionMediaDTO>> obtenerMediasPorEquipo(@PathVariable Integer equipoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        List<EvaluacionMediaDTO> medias = evaluacionService.obtenerMediasPorEquipo(equipoId);
        return ResponseEntity.ok(medias);
    }

    @GetMapping("/equipo/{equipoId}/evaluacion-activa")
    public ResponseEntity<Boolean> isEvaluacionActiva(@PathVariable Integer equipoId) {
        boolean activa = evaluacionService.isEvaluacionActiva(equipoId);
        return ResponseEntity.ok(activa);
    }

    @GetMapping("/equipo/{equipoId}/evaluacion-activa-id")
    public ResponseEntity<Integer> obtenerEvaluacionActivaId(@PathVariable Integer equipoId) {
        Integer evaluacionId = evaluacionService.obtenerEvaluacionActiva(equipoId);
        if (evaluacionId != null) {
            return ResponseEntity.ok(evaluacionId);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/equipo/{equipoId}/evaluacion-realizada/{estudianteId}")
    public ResponseEntity<Boolean> isEvaluacionRealizada(@PathVariable Integer equipoId, @PathVariable Integer estudianteId) {
        boolean realizada = evaluacionDetalleRepository.existsByEquipoIdAndEvaluadorId(equipoId, estudianteId);
        return ResponseEntity.ok(realizada);
    }

    @PreAuthorize("hasAuthority('ESTUDIANTE')")
    @PostMapping("/estudiante/crear")
    public ResponseEntity<String> crearEvaluacionDetalle(@RequestBody EvaluacionDetalleRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        evaluacionService.crearEvaluacionDetalle(request.getEvaluacionId(), request.getEvaluadorId(), request.getDetalles());
        return ResponseEntity.ok("Evaluación guardada correctamente");
    }

}
