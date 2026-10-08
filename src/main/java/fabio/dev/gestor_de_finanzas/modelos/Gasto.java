package fabio.dev.gestor_de_finanzas.modelos;

import fabio.dev.gestor_de_finanzas.utilitis.Utilitis;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El titulo del gasto es necesaria, por lo cual no puede estar vació")
    @Column(unique = true, nullable = false, length = 250)
    private String tituloGasto;

    @Column(length = 250)
    private String descripcionGasto;

    @NotNull(message = "La cantidad del gasto es necesaria, por lo cual no puede estar vacío")
    @Positive(message = "La cantidad del gasto debe de ser mayor a cero")
    private Double cantidad;

    @ElementCollection
    @CollectionTable(name = "gasto_categoria", joinColumns = @JoinColumn(name = "gasto_id", referencedColumnName = "id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria")
    private List<Enum<CategoriasPredefinidas>> categorias = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", referencedColumnName = "id")
    private Cuenta cuenta;

    @NotNull(message = "La fecha del gasto realizado es necesario, por lo cual no debe de estar vacío")
    private LocalDate fechaGasto;

    @PrePersist
    void onCreate(){
        this.fechaGasto = Utilitis.GenerarFecha();
    }

    public void agregarCategoriaPre(Enum<CategoriasPredefinidas> categoria) {
        categorias.add(categoria);
        //categoria.getGastos().add(this);
    }

    public void removerCategoriaPre(Enum<CategoriasPredefinidas> categoria) {
        categorias.remove(categoria);
        //categoria.getGastos().remove(this);
    }

}