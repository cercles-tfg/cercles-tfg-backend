package tfg.backend_tfg.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tfg.backend_tfg.dto.CursoDetalleDTO;
import tfg.backend_tfg.dto.CursoSummaryDTO;
import tfg.backend_tfg.dto.EquipoDTO;
import tfg.backend_tfg.dto.EstudianteDTO;
import tfg.backend_tfg.model.Curso;
import tfg.backend_tfg.model.CursoRequest;
import tfg.backend_tfg.model.Equipo;
import tfg.backend_tfg.model.Estudiante;
import tfg.backend_tfg.model.EstudianteCurso;
import tfg.backend_tfg.model.EstudianteRequest;
import tfg.backend_tfg.model.Evaluacion;
import tfg.backend_tfg.model.EvaluacionRequest;
import tfg.backend_tfg.model.Profesor;
import tfg.backend_tfg.model.ProfesorCurso;
import tfg.backend_tfg.model.ProfesorRequest;
import tfg.backend_tfg.model.Rol;
import tfg.backend_tfg.model.Usuario;
import tfg.backend_tfg.model.UsuarioRequest;
import tfg.backend_tfg.repository.CursoRepository;
import tfg.backend_tfg.repository.EquipoRepository;
import tfg.backend_tfg.repository.EstudianteCursoRepository;
import tfg.backend_tfg.repository.EstudianteEquipoRepository;
import tfg.backend_tfg.repository.EvaluacionRepository;
import tfg.backend_tfg.repository.ProfesorCursoRepository;
import tfg.backend_tfg.repository.UsuarioRepository;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private EstudianteCursoRepository estudianteCursoRepository;

    @Autowired
    private EstudianteEquipoRepository estudianteEquipoRepository;

    @Autowired
    private ProfesorCursoRepository profesorCursoRepository;

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    //Leer estudiantes del excel
    public ResponseEntity<?> uploadEstudiantes(MultipartFile file) {
        try {
            if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Por favor sube un archivo Excel válido.");
            }

            List<Map<String, String>> estudiantes = new ArrayList<>();
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String cognomsNom = row.getCell(0).getStringCellValue();
                    String correo = row.getCell(1).getStringCellValue();

                    String[] partes = cognomsNom.split(",");
                    if (partes.length < 2) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato incorrecto en la fila " + (i + 1));
                    }

                    String apellidos = partes[0].trim();
                    String nombre = partes[1].trim();
                    String nombreCompleto = nombre + " " + apellidos;

                    Map<String, String> estudianteData = new HashMap<>();
                    estudianteData.put("nombre", nombreCompleto);
                    estudianteData.put("correo", correo);
                    estudiantes.add(estudianteData);
                }
            }
            workbook.close();
            return ResponseEntity.ok(estudiantes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el archivo: " + e.getMessage());
        }
    }

    //Crear curso
    public ResponseEntity<?> crearCurso(CursoRequest cursoRequest, String profesorEmail) {
        try {
            // Verificar si ya existe un curso similar activo
            Optional<Curso> cursoExistente = cursoRepository.findByNombreAsignaturaAndAñoInicioAndCuatrimestreAndActivo(
                    cursoRequest.getNombreAsignatura(), cursoRequest.getAñoInicio(), cursoRequest.getCuatrimestre(), true);

            if (cursoExistente.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Curso activo ya existente. ¿Desea desactivarlo?");
            }

            // Crear el curso
            Curso curso = Curso.builder()
                    .nombreAsignatura(cursoRequest.getNombreAsignatura())
                    .añoInicio(cursoRequest.getAñoInicio())
                    .cuatrimestre(cursoRequest.getCuatrimestre())
                    .activo(cursoRequest.isActivo())
                    .build();

            curso = cursoRepository.save(curso);

            // Asignar profesores al curso
            ResponseEntity<?> responseProfesores = añadirProfesores(curso, cursoRequest.getProfesores());
            if (!responseProfesores.getStatusCode().equals(HttpStatus.OK)) {
                return responseProfesores;
            }

            // Asignar estudiantes al curso
            ResponseEntity<?> responseEstudiantes = añadirEstudiantes(curso, cursoRequest.getEstudiantes());
            if (!responseEstudiantes.getStatusCode().equals(HttpStatus.OK)) {
                return responseEstudiantes;
            }

            // Crear evaluaciones
            ResponseEntity<?> responseEvaluaciones = crearEvaluaciones(curso, cursoRequest.getPeriodosEvaluacion());
            if (!responseEvaluaciones.getStatusCode().equals(HttpStatus.OK)) {
                return responseEvaluaciones;
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Curso creado exitosamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el curso: " + e.getMessage());
        }
    }

    //Crear evaluaciones
    public ResponseEntity<?> crearEvaluaciones(Curso curso, List<EvaluacionRequest> evaluacionRequests) {
    try {
        // Validar si las fechas de las evaluaciones son consistentes
        for (EvaluacionRequest evaluacionRequest : evaluacionRequests) {
            if (evaluacionRequest.getFechaFin().isBefore(evaluacionRequest.getFechaInicio())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La fecha de fin no puede ser anterior a la fecha de inicio en las evaluaciones.");
            }
        }

        // Crear las evaluaciones y asociarlas al curso
        List<Evaluacion> evaluaciones = evaluacionRequests.stream()
                .map(evaluacionRequest -> {
                    Evaluacion evaluacion = new Evaluacion();
                    evaluacion.setFechaInicio(evaluacionRequest.getFechaInicio());
                    evaluacion.setFechaFin(evaluacionRequest.getFechaFin());
                    evaluacion.setCurso(curso);
                    return evaluacion;
                })
                .toList();

        // Guardar todas las evaluaciones en la base de datos
        evaluacionRepository.saveAll(evaluaciones);

        return ResponseEntity.ok("Evaluaciones creadas correctamente.");
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear las evaluaciones: " + e.getMessage());
    }
}

    //Cambiar estado curso
    public ResponseEntity<?> cambiarEstadoCurso(CursoRequest cursoRequest) {
        try {
            Optional<Curso> cursoOpt = Optional.empty();
            if (cursoRequest.getId() != null && cursoRequest.getId() != 0) {
                cursoOpt = cursoRepository.findById(cursoRequest.getId());
            } else {
                cursoOpt = cursoRepository.findByNombreAsignaturaAndAñoInicioAndCuatrimestreAndActivo(
                        cursoRequest.getNombreAsignatura(), cursoRequest.getAñoInicio(), cursoRequest.getCuatrimestre(), true);
            }

            if (cursoOpt.isPresent()) {
                Curso curso = cursoOpt.get();
                curso.setActivo(!curso.isActivo());
                cursoRepository.save(curso);

                String estado = curso.isActivo() ? "activado" : "desactivado";
                return ResponseEntity.ok("Curso " + estado + " exitosamente");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Curso no encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cambiar el estado del curso: " + e.getMessage());
        }
    }

    //verificar curso igual ya existente
    public ResponseEntity<?> verificarCursoExistente(CursoRequest cursoRequest) {
        try {
            Optional<Curso> cursoOpt = cursoRepository.findByNombreAsignaturaAndAñoInicioAndCuatrimestreAndActivo(
                    cursoRequest.getNombreAsignatura(), cursoRequest.getAñoInicio(), cursoRequest.getCuatrimestre(), true);

            if (cursoOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Curso activo ya existente. ¿Desea desactivarlo?");
            }
            return ResponseEntity.ok("No hay conflicto");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al verificar el curso existente: " + e.getMessage());
        }
    }

    //obtener cursos de un profesor
    public List<CursoSummaryDTO> obtenerCursosProfesor(String profesorEmail) {
        Profesor profesor = (Profesor) usuarioRepository.findByCorreo(profesorEmail)
                .orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado"));

        List<ProfesorCurso> profesorCursos = profesorCursoRepository.findByProfesorId(profesor.getId());
        return profesorCursos.stream()
                .map(profesorCurso -> {
                    Curso curso = profesorCurso.getCurso();
                    int numeroEstudiantes = estudianteCursoRepository.countByCursoId(curso.getId());
                    return new CursoSummaryDTO(
                            curso.getId(),
                            curso.getNombreAsignatura(),
                            curso.getAñoInicio(),
                            curso.getCuatrimestre(),
                            curso.isActivo(),
                            numeroEstudiantes
                    );
                })
                .toList();
    }

    // Obtener detalle de un curso
    public CursoDetalleDTO obtenerDetalleCurso(Integer cursoId, String profesorEmail) {
        // Verificar que el profesor existe
        Profesor profesor = (Profesor) usuarioRepository.findByCorreo(profesorEmail)
                .orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado"));

        // Verificar que el profesor tiene acceso al curso
        boolean accesoPermitido = profesorCursoRepository.findByProfesorIdAndCursoId(profesor.getId(), cursoId).isPresent();
        if (!accesoPermitido) {
            throw new IllegalArgumentException("No tiene acceso a este curso.");
        }

        // Obtener el curso
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));

        // Obtener estudiantes del curso
        List<EstudianteCurso> estudiantesCurso = estudianteCursoRepository.findByCursoId(cursoId);
        List<Integer> estudiantesConEquipoIds = estudianteEquipoRepository.findByCursoId(cursoId).stream()
                .map(estudianteEquipo -> estudianteEquipo.getEstudiante().getId())
                .toList();

        // Estudiantes sin grupo
        List<EstudianteDTO> estudiantesSinGrupo = estudiantesCurso.stream()
                .map(EstudianteCurso::getEstudiante)
                .filter(estudiante -> !estudiantesConEquipoIds.contains(estudiante.getId()))
                .map(estudiante -> new EstudianteDTO(estudiante.getId(), estudiante.getNombre(), estudiante.getCorreo()))
                .toList();

        // Equipos con miembros
        List<EquipoDTO> equiposConMiembros = equipoRepository.findByCursoId(cursoId).stream()
                .map(equipo -> new EquipoDTO(
                        equipo.getNombre(),
                        equipo.getId(),
                        equipo.getEvaluador() != null ? equipo.getEvaluador().getId() : null,
                        estudianteEquipoRepository.findByEquipoId(equipo.getId()).stream()
                                .map(estudianteEquipo -> estudianteEquipo.getEstudiante().getNombre())
                                .toList()
                ))
                .toList();

        // Nombres de los profesores
        List<String> nombresProfesores = profesorCursoRepository.findByCursoId(cursoId).stream()
                .map(profesorCurso -> profesorCurso.getProfesor().getNombre())
                .toList();

        // Crear y retornar el DTO
        return new CursoDetalleDTO(
                curso.getId(),
                curso.getNombreAsignatura(),
                curso.getAñoInicio(),
                curso.getCuatrimestre(),
                curso.isActivo(),
                estudiantesSinGrupo.stream().map(EstudianteDTO::getNombre).toList(),
                estudiantesSinGrupo.stream().map(EstudianteDTO::getCorreo).toList(),
                nombresProfesores,
                equiposConMiembros
        );
    }


    //Modificar datos basicos curso
    public ResponseEntity<?> modificarDatosCurso(Curso cursoExistente, CursoRequest cursoRequest) {
        if (!cursoExistente.getNombreAsignatura().equals(cursoRequest.getNombreAsignatura()) ||
            cursoExistente.getAñoInicio() != cursoRequest.getAñoInicio() ||
            cursoExistente.getCuatrimestre() != cursoRequest.getCuatrimestre()) {
    
            Optional<Curso> cursoConflicto = cursoRepository.findByNombreAsignaturaAndAñoInicioAndCuatrimestreAndActivo(
                cursoRequest.getNombreAsignatura(), cursoRequest.getAñoInicio(), cursoRequest.getCuatrimestre(), true
            );
    
            if (cursoConflicto.isPresent() && cursoConflicto.get().getId() != (cursoExistente.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe un curso activo con el mismo nombre, año y cuatrimestre.");
            }
        }
    
        // Actualizar los datos básicos
        cursoExistente.setNombreAsignatura(cursoRequest.getNombreAsignatura());
        cursoExistente.setAñoInicio(cursoRequest.getAñoInicio());
        cursoExistente.setCuatrimestre(cursoRequest.getCuatrimestre());
    
        return ResponseEntity.ok().build();
    }

    // Función para añadir estudiantes al curso
    public ResponseEntity<?> añadirEstudiantes(Curso cursoExistente, List<EstudianteRequest> estudiantesAñadir) {
        for (EstudianteRequest estudianteRequest : estudiantesAñadir) {
            Usuario usuarioExistente = usuarioRepository.findByCorreo(estudianteRequest.getCorreo()).orElse(null);
            if (usuarioExistente == null) {
                // Llamar al servicio para crear el usuario
                UsuarioRequest usuarioRequest = new UsuarioRequest();
                usuarioRequest.setCorreo(estudianteRequest.getCorreo());
                usuarioRequest.setNombre(estudianteRequest.getNombre());
                usuarioRequest.setRol(Rol.Estudiante);

                ResponseEntity<?> response = usuarioService.crearUsuario(usuarioRequest);

                if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error al crear el estudiante: " + estudianteRequest.getCorreo());
                }

                usuarioExistente = usuarioRepository.findByCorreo(estudianteRequest.getCorreo()).orElse(null);
            }
            // Asociar al estudiante con el curso
            EstudianteCurso estudianteCurso = new EstudianteCurso((Estudiante) usuarioExistente, cursoExistente);
            estudianteCursoRepository.save(estudianteCurso);
        }
        return ResponseEntity.ok().build();
    }

    // Función para borrar estudiantes del curso
    public void borrarEstudiantes(Curso cursoExistente, List<EstudianteRequest> estudiantesBorrar) {
        for (EstudianteRequest estudianteRequest : estudiantesBorrar) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(estudianteRequest.getCorreo());

            if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) usuarioOpt.get();

                // Eliminar la relación entre el estudiante y el curso
                Optional<EstudianteCurso> estudianteCursoOpt = estudianteCursoRepository.findByEstudianteAndCurso(estudiante, cursoExistente);
                if (estudianteCursoOpt.isPresent()) {
                    estudianteCursoRepository.delete(estudianteCursoOpt.get());
                }

                // Verificar si el estudiante no está en ningún otro curso
                List<EstudianteCurso> cursosDelEstudiante = estudianteCursoRepository.findByEstudianteId(estudiante.getId());
                if (cursosDelEstudiante.isEmpty()) {
                    usuarioRepository.delete(estudiante);
                }
            }
        }
    }

    // Función para añadir profesores al curso
    public ResponseEntity<?> añadirProfesores(Curso cursoExistente, List<ProfesorRequest> profesoresAñadir) {
        for (ProfesorRequest profesorRequest : profesoresAñadir) {
            Usuario usuarioExistente = usuarioRepository.findByCorreo(profesorRequest.getCorreo()).orElse(null);
            
            if (usuarioExistente == null) {
                // Crear un nuevo profesor si no existe
                UsuarioRequest usuarioRequest = new UsuarioRequest();
                usuarioRequest.setCorreo(profesorRequest.getCorreo());
                usuarioRequest.setNombre(profesorRequest.getNombre());
                usuarioRequest.setRol(Rol.Profesor);

                ResponseEntity<?> response = usuarioService.crearUsuario(usuarioRequest);
                if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error al crear el profesor: " + profesorRequest.getCorreo());
                }

                usuarioExistente = usuarioRepository.findByCorreo(profesorRequest.getCorreo()).orElse(null);
            }

            // Crear la relación ProfesorCurso si no existe
            Profesor profesor = (Profesor) usuarioExistente;
            Optional<ProfesorCurso> profesorCursoOpt = profesorCursoRepository.findByProfesorIdAndCursoId(profesor.getId(), cursoExistente.getId());
            if (profesorCursoOpt.isEmpty()) {
                ProfesorCurso profesorCurso = new ProfesorCurso(profesor, cursoExistente);
                profesorCursoRepository.save(profesorCurso);
            }
        }
        return ResponseEntity.ok().build();
    }


    // Función para borrar profesores del curso
    public void borrarProfesores(Curso cursoExistente, List<ProfesorRequest> profesoresBorrar) {
        List<Integer> idsProfesoresBorrados = new ArrayList<>();

        for (ProfesorRequest profesorRequest : profesoresBorrar) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(profesorRequest.getCorreo());
            if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Profesor) {
                Profesor profesor = (Profesor) usuarioOpt.get();

                // Buscar la relación ProfesorCurso y eliminarla si existe
                Optional<ProfesorCurso> profesorCursoOpt = profesorCursoRepository.findByProfesorIdAndCursoId(profesor.getId(), cursoExistente.getId());
                if (profesorCursoOpt.isPresent()) {
                    profesorCursoRepository.delete(profesorCursoOpt.get());
                    
                    // Agregar el ID del profesor a la lista de IDs a borrar del curso
                    idsProfesoresBorrados.add(profesor.getId());
                }
            } else {
                System.out.println("El profesor con correo " + profesorRequest.getCorreo() + " no fue encontrado o no es un profesor.");
            }
        }

        // Actualizar la lista de profesores del curso eliminando todos los profesores borrados
        cursoExistente.getProfesores().removeIf(profesor -> idsProfesoresBorrados.contains(profesor.getId()));
    }
    

    public Map<String, List<EstudianteDTO>> obtenerEstudiantesPorCurso(int cursoId) {
        // Obtener todos los estudiantes del curso
        List<EstudianteCurso> estudiantesCurso = estudianteCursoRepository.findByCursoId(cursoId);
    
        // Crear listas separadas para estudiantes con equipo y sin equipo
        List<EstudianteDTO> estudiantesConEquipo = new ArrayList<>();
        List<EstudianteDTO> estudiantesSinEquipo = new ArrayList<>();
    
        for (EstudianteCurso ec : estudiantesCurso) {
            int estudianteId = ec.getEstudiante().getId();
            boolean tieneEquipo = !estudianteEquipoRepository.findByEstudianteIdAndCursoId(estudianteId, cursoId).isEmpty();
    
            String nombre = ec.getEstudiante().getNombre();
            String correo = ec.getEstudiante().getCorreo();
    
            EstudianteDTO estudianteDTO = new EstudianteDTO(estudianteId, nombre, correo);
    
            if (tieneEquipo) {
                estudiantesConEquipo.add(estudianteDTO);
            } else {
                estudiantesSinEquipo.add(estudianteDTO);
            }
        }
    
        // Crear el mapa de respuesta
        Map<String, List<EstudianteDTO>> respuesta = new HashMap<>();
        respuesta.put("conEquipo", estudiantesConEquipo);
        respuesta.put("sinEquipo", estudiantesSinEquipo);
    
        return respuesta;
    }
    

    public List<EstudianteDTO> obtenerProfesoresDelCurso(int cursoId) {
        // Obtener la lista de profesores del curso
        return profesorCursoRepository.findByCursoId(cursoId).stream()
                .map(profesorCurso -> {
                    Profesor profesor = profesorCurso.getProfesor();
                    return new EstudianteDTO(
                            profesor.getId(),
                            profesor.getNombre(),
                            profesor.getCorreo()
                    );
                })
                .toList();
    }

    public void borrarCurso(int cursoId) {
        // Verificar si el curso existe
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado."));

        // Eliminar el curso
        cursoRepository.delete(curso);
    }
}
