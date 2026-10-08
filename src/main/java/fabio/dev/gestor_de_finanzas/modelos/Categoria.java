package fabio.dev.gestor_de_finanzas.modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre de la categoria es necesario, por lo cual no debe estar vacío")
    @Column(unique = true, nullable = false, length = 200)
    private String nombreCategoria;

    @Transient
    private List<Gasto> gastos = new ArrayList<>();

    public void agregarGasto(Gasto gasto) {
        gastos.add(gasto);
        //gasto.getCategorias().add(this);
    }

    public void removerGasto(Gasto gasto) {
        gastos.remove(gasto);
        //gasto.getCategorias().remove(this);
    }

}
