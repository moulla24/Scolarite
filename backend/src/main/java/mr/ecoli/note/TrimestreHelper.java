package mr.ecoli.note;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import mr.ecoli.dao.entities.AnneeScolaire;
import org.springframework.stereotype.Component;

@Component
public class TrimestreHelper {

    public int trimestreCourant(AnneeScolaire annee) {
        LocalDate today = LocalDate.now();
        LocalDate debut = annee.getDateDebut();
        LocalDate fin = annee.getDateFin();
        if (today.isBefore(debut)) {
            return 1;
        }
        if (!today.isBefore(fin)) {
            return 3;
        }
        long totalDays = ChronoUnit.DAYS.between(debut, fin) + 1;
        long elapsed = ChronoUnit.DAYS.between(debut, today);
        long third = Math.max(totalDays / 3, 1);
        if (elapsed < third) {
            return 1;
        }
        if (elapsed < third * 2) {
            return 2;
        }
        return 3;
    }

    public String libelle(int trimestre) {
        return "Trimestre " + trimestre;
    }

    public LocalDate dateReference(AnneeScolaire annee, int trimestre) {
        LocalDate debut = annee.getDateDebut();
        LocalDate fin = annee.getDateFin();
        long totalDays = ChronoUnit.DAYS.between(debut, fin);
        long offset = switch (trimestre) {
            case 1 -> totalDays / 6;
            case 2 -> totalDays / 2;
            default -> (totalDays * 5) / 6;
        };
        LocalDate date = debut.plusDays(offset);
        return date.isAfter(fin) ? fin : date;
    }
}
