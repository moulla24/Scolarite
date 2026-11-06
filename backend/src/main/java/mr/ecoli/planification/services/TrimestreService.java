package mr.ecoli.planification.services;

import java.util.List;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.dao.entities.Trimestre;
import mr.ecoli.dao.repositories.TrimestreRepository;
import mr.ecoli.planification.dtos.TrimestreDto;
import mr.ecoli.planification.dtos.TrimestreRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TrimestreService {

    private final TrimestreRepository trimestreRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;
    private final SemaineService semaineService;

    public TrimestreService(
            TrimestreRepository trimestreRepository,
            EntityMapper mapper,
            EntityReferenceService refs,
            SemaineService semaineService) {
        this.trimestreRepository = trimestreRepository;
        this.mapper = mapper;
        this.refs = refs;
        this.semaineService = semaineService;
    }

    @Transactional(readOnly = true)
    public List<TrimestreDto> listTrimestres(Long anneeScolaireId) {
        Long yearId = anneeScolaireId != null ? anneeScolaireId : refs.activeAnnee().getId();
        return trimestreRepository.findAllFiltered(yearId).stream()
                .map(mapper::toTrimestreDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrimestreDto getTrimestre(Long id) {
        return mapper.toTrimestreDto(refs.requireTrimestre(id));
    }

    public TrimestreDto createTrimestre(TrimestreRequest request) {
        validateDates(request.dateDebut(), request.dateFin());
        ensureUniqueNumero(request.anneeScolaireId(), request.numero(), null);
        Trimestre t = new Trimestre();
        apply(t, request);
        t = trimestreRepository.save(t);
        semaineService.ensureGeneratedForTrimestre(t);
        return mapper.toTrimestreDto(t);
    }

    public TrimestreDto updateTrimestre(Long id, TrimestreRequest request) {
        validateDates(request.dateDebut(), request.dateFin());
        ensureUniqueNumero(request.anneeScolaireId(), request.numero(), id);
        Trimestre t = refs.requireTrimestre(id);
        apply(t, request);
        return mapper.toTrimestreDto(trimestreRepository.save(t));
    }

    public void deleteTrimestre(Long id) {
        trimestreRepository.delete(refs.requireTrimestre(id));
    }

    private void apply(Trimestre t, TrimestreRequest request) {
        t.setAnneeScolaire(refs.requireAnneeScolaire(request.anneeScolaireId()));
        t.setNumero(request.numero());
        t.setNom(request.nom().trim());
        t.setDateDebut(request.dateDebut());
        t.setDateFin(request.dateFin());
        t.setActif(request.actif());
    }

    private void validateDates(java.time.LocalDate debut, java.time.LocalDate fin) {
        if (fin.isBefore(debut)) {
            throw new BadRequestException("La date de fin doit être postérieure à la date de début.");
        }
    }

    private void ensureUniqueNumero(Long anneeScolaireId, Short numero, Long excludeId) {
        if (!trimestreRepository.existsByAnneeScolaireIdAndNumero(anneeScolaireId, numero)) {
            return;
        }
        if (excludeId != null) {
            Trimestre existing = refs.requireTrimestre(excludeId);
            if (existing.getAnneeScolaire().getId().equals(anneeScolaireId)
                    && existing.getNumero().equals(numero)) {
                return;
            }
        }
        throw new BadRequestException("Un trimestre n°" + numero + " existe déjà pour cette année scolaire.");
    }
}
