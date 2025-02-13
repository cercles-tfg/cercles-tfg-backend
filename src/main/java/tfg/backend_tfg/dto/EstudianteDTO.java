package tfg.backend_tfg.dto;

public class EstudianteDTO {
    private int id;
    private String nombre;
    private String correo;
    private String grupo;

    // Constructor
    public EstudianteDTO(int id, String nombre, String correo, String grupo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.grupo = grupo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

}
