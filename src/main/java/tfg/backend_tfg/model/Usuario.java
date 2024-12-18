package tfg.backend_tfg.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.lang.classfile.constantpool.IntegerEntry;
import java.util.Collection;
import java.util.List;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "rol"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Estudiante.class, name = "estudiante"),
    @JsonSubTypes.Type(value = Profesor.class, name = "profesor")
})
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "rol")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "git_username")
    private String gitUsername;

    @Column(name = "taiga_username")
    private String taigaUsername;

    @Column(name = "git_user_id")
    private Integer gitId;

    @Column(name = "taiga_user_id")
    private Integer taigaId;

    @Column(name = "github_token")
    private String githubAccessToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", insertable = false, updatable = false) // Evitamos duplicar la columna con el discriminador
    private Rol rol;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rol.name()));
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getPassword() {
        return null; // Auth con google no requiere guardar la contraseña
    }
}
