package mr.ecoli.planification.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.Semaine;
import mr.ecoli.dao.entities.Trimestre;
import mr.ecoli.dao.repositories.SemaineRepository;
import mr.ecoli.planification.dtos.SemaineDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SemaineService {

    private static final DateTimeFormatter DAY_FMT =
            DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH);

    private final SemaineRepository semaineRepository;
    private final EntityReferenceService refs;

    public SemaineService(SemaineRepository semaineRepository, EntityReferenceService refs) {
        this.semaineRepository = semaineRepository;
        this.refs = refs;
    }

    @Transactional(readOnly = true)
    public List<SemaineDto> listByTrimestre(Long trimestreId) {
        refs.requireTrimestre(trimestreId);
        ensureGenerated(trimestreId);
        return semaineRepository.findByTrimestreIdOrderByNumeroAsc(trimestreId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Long> resolveCurrentSemaineId() {
        return semaineRepository.findCurrentForDate(LocalDate.now()).map(Semaine::getId);
    }

    @Transactional(readOnly = true)
    public Optional<Long> resolveCurrentSemaineIdForTrimestre(Long trimestreId) {
        return semaineRepository.findByTrimestreIdAndDate(trimestreId, LocalDate.now())
                .map(Semaine::getId);
    }

    public void ensureGeneratedForTrimestre(Trimestre trimestre) {
        if (semaineRepository.existsByTrimestreId(trimestre.getId())) {
            return;
        }
        semaineRepository.saveAll(SemaineGenerator.buildForTrimestre(trimestre));
    }

    private void ensureGenerated(Long trimestreId) {
        if (!semaineRepository.existsByTrimestreId(trimestreId)) {
            ensureGeneratedForTrimestre(refs.requireTrimestre(trimestreId));
        }
    }

    private SemaineDto toDto(Semaine semaine) {
        return new SemaineDto(
                semaine.getId(),
                semaine.getTrimestre().getId(),
                semaine.getNumero(),
                semaine.getDateDebut(),
                semaine.getDateFin(),
                formatLabel(semaine));
    }

    private String formatLabel(Semaine semaine) {
        String start = semaine.getDateDebut().format(DAY_FMT);
        String end = semaine.getDateFin().format(DAY_FMT);
        return "Semaine " + semaine.getNumero() + " — " + start + " au " + end;
    }
}
