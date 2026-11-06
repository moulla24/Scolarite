package mr.ecoli.seance.services;

import java.util.List;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.seance.dtos.SeanceEmploiDto;
import mr.ecoli.seance.dtos.SeanceRequest;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.SeancePresence;
import mr.ecoli.dao.repositories.EnseignementRepository;
import mr.ecoli.dao.repositories.SeancePresenceRepository;
import mr.ecoli.planification.services.SemaineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmploiDuTempsService {

    private final SeancePresenceRepository repository;
    private final EnseignementRepository enseignementRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;
    private final SemaineService semaineService;

    public EmploiDuTempsService(
            SeancePresenceRepository repository,
            EnseignementRepository enseignementRepository,
            EntityMapper mapper,
            EntityReferenceService refs,
            SemaineService semaineService) {
        this.repository = repository;
        this.enseignementRepository = enseignementRepository;
        this.mapper = mapper;
        this.refs = refs;
        this.semaineService = semaineService;
    }

    @Transactional(readOnly = true)
    public List<SeanceEmploiDto> listSeances(Long classeId, Long semaineId) {
        Long resolvedSemaineId = resolveSemaineId(semaineId);
        if (resolvedSemaineId == null) {
            return List.of();
        }
        return repository.findByFilters(classeId, resolvedSemaineId).stream()
                .map(mapper::toSeanceDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeanceEmploiDto getById(Long id) {
        return mapper.toSeanceDto(refs.requireSeance(id));
    }

    public SeanceEmploiDto create(SeanceRequest request) {
        ensureEnseignementValide(request);
        ensureSlotAvailable(request, null);
        SeancePresence s = new SeancePresence();
        apply(s, request);
        return mapper.toSeanceDto(repository.save(s));
    }

    public SeanceEmploiDto update(Long id, SeanceRequest request) {
        ensureEnseignementValide(request);
        ensureSlotAvailable(request, id);
        SeancePresence s = refs.requireSeance(id);
        apply(s, request);
        return mapper.toSeanceDto(repository.save(s));
    }

    public void delete(Long id) {
        repository.delete(refs.requireSeance(id));
    }

    private Long resolveSemaineId(Long semaineId) {
        if (semaineId != null) {
            return semaineId;
        }
        return semaineService.resolveCurrentSemaineId().orElse(null);
    }

    private void ensureEnseignementValide(SeanceRequest request) {
        if (!enseignementRepository.existsByClasseIdAndMatiereIdAndEnseignantId(
                request.classeId(), request.matiereId(), request.enseignantId())) {
            throw new BadRequestException(
                    "Cet enseignant n'est pas affecté à cette matière dans cette classe.");
        }
    }

    private void ensureSlotAvailable(SeanceRequest request, Long excludeId) {
        boolean exists = repository.existsBySemaineIdAndClasseIdAndJourSemaineAndHeureDebut(
                request.semaineId(),
                request.classeId(),
                request.jourSemaine(),
                request.heureDebut());
        if (!exists) {
            return;
        }
        if (excludeId != null) {
            SeancePresence current = refs.requireSeance(excludeId);
            if (current.getSemaine().getId().equals(request.semaineId())
                    && current.getClasse().getId().equals(request.classeId())
                    && current.getJourSemaine().equals(request.jourSemaine())
                    && current.getHeureDebut().equals(request.heureDebut())) {
                return;
            }
        }
        throw new BadRequestException("Ce créneau est déjà occupé pour cette semaine et cette classe.");
    }

    private void apply(SeancePresence s, SeanceRequest request) {
        s.setSemaine(refs.requireSemaine(request.semaineId()));
        s.setClasse(refs.requireClasse(request.classeId()));
        s.setMatiere(refs.requireMatiere(request.matiereId()));
        s.setEnseignant(refs.requireEnseignant(request.enseignantId()));
        s.setJourSemaine(request.jourSemaine());
        s.setHeureDebut(request.heureDebut());
        s.setHeureFin(request.heureFin());
        s.setSalle(request.salle());
        s.setType(request.type());
    }
}
