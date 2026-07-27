package fabio.dev.gestor_de_finanzas.modelos;

import fabio.dev.gestor_de_finanzas.utilitis.Utilitis;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Ingreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El titulo del ingreso es necesario, por lo cual no puede estar vacío")
    @Column(unique = true, nullable = false, length = 220)
    private String tituloIngreso;

    @Column(length = 250)
    private String descripcionIngreso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", referencedColumnName = "id")
    private Cuenta cuenta;

    @NotNull(message = "La cantidad del ingreso es necesario, por lo cual no puede estar vacío")
    @Positive(message = "La cantidad del ingreso debe ser mayor a cero")
    private Double cantidadIngreso;

    @NotNull(message = "La fecha no puede estar vacío")
    private LocalDate fechaIngreso;

    @PrePersist
    public void prePersist() {
        this.fechaIngreso = Utilitis.GenerarFecha();
    }

}