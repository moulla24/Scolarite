package mr.ecoli.planification.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import mr.ecoli.dao.entities.Semaine;
import mr.ecoli.dao.entities.Trimestre;

public final class SemaineGenerator {

    private SemaineGenerator() {}

    public static List<Semaine> buildForTrimestre(Trimestre trimestre) {
        LocalDate trimStart = trimestre.getDateDebut();
        LocalDate trimEnd = trimestre.getDateFin();
        LocalDate monday = trimStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<Semaine> weeks = new ArrayList<>();
        short weekNumber = 1;
        while (!monday.isAfter(trimEnd)) {
            LocalDate friday = monday.plusDays(4);
            if (!friday.isBefore(trimStart) && !monday.isAfter(trimEnd)) {
                Semaine semaine = new Semaine();
                semaine.setTrimestre(trimestre);
                semaine.setNumero(weekNumber);
                semaine.setDateDebut(monday);
                semaine.setDateFin(friday);
                weeks.add(semaine);
                weekNumber++;
            }
            monday = monday.plusDays(7);
        }
        return weeks;
    }
}
