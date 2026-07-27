package fabio.dev.gestor_de_finanzas.utilitis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Utilitis {

    public static LocalDate GenerarFecha(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return  LocalDate.parse(LocalDate.now().format(formatter), formatter);
    }
}
