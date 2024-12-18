package tfg.backend_tfg.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@Table(name = "equipo")
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluador_id", nullable = false)
    private Profesor evaluador;

    @Column(name = "git_organizacion")
    private String gitOrganizacion;

    @Column(name = "taiga_proyecto")
    private String taigaProyecto;

    @Column(name = "git_org_id")
    private String gitOrganizacionId;

    @Column(name = "taiga_project_id")
    private String taigaProyectoId;

    @Column(name = "taiga_refresh_token")
    private String taigaRefreshToken;

    @Column(nullable = false)
    private boolean validado;

    @Column(name = "github_app_instalada", nullable = false)
    private boolean githubAppInstalada;

}
