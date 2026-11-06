package mr.ecoli.anneescolaire.services;

import mr.ecoli.dao.entities.AnneeScolaire;
import mr.ecoli.dao.repositories.AnneeScolaireRepository;
import mr.ecoli.anneescolaire.dtos.AnneeScolaireDto;
import mr.ecoli.anneescolaire.dtos.AnneeScolaireRequest;

import java.util.List;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.common.exception.ResourceNotFoundException;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.dao.repositories.InscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnneeScolaireService {

    private final AnneeScolaireRepository anneeScolaireRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;

    public AnneeScolaireService(
            AnneeScolaireRepository anneeScolaireRepository,
            InscriptionRepository inscriptionRepository,
            EntityMapper mapper,
            EntityReferenceService refs) {
        this.anneeScolaireRepository = anneeScolaireRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.mapper = mapper;
        this.refs = refs;
    }
    @Transactional(readOnly = true)
    public List<AnneeScolaireDto> list() {
        return anneeScolaireRepository.findAllByOrderByDateDebutDesc().stream()
                .map(mapper::toAnneeScolaireDto)
                .toList();
    }
    @Transactional(readOnly = true)
    public AnneeScolaireDto getById(Long id) {
        return mapper.toAnneeScolaireDto(requireAnnee(id));
    }
    public AnneeScolaireDto create(AnneeScolaireRequest request) {
        validateDates(request);
        AnneeScolaire annee = new AnneeScolaire();
        annee.setEcole(refs.requireEcole(request.ecoleId()));
        applyRequest(annee, request);
        if (request.active()) {
            deactivateOthersForEcole(request.ecoleId(), null);
        }
        return mapper.toAnneeScolaireDto(anneeScolaireRepository.save(annee));
    }
    public AnneeScolaireDto update(Long id, AnneeScolaireRequest request) {
        validateDates(request);
        AnneeScolaire annee = requireAnnee(id);
        annee.setEcole(refs.requireEcole(request.ecoleId()));
        applyRequest(annee, request);
        if (request.active()) {
            deactivateOthersForEcole(request.ecoleId(), id);
        }
        return mapper.toAnneeScolaireDto(anneeScolaireRepository.save(annee));
    }
    public void delete(Long id) {
        requireAnnee(id);
        if (inscriptionRepository.existsByAnneeScolaire_Id(id)) {
            throw new BadRequestException(
                    "Impossible de supprimer cette année scolaire : des inscriptions y sont rattachées.");
        }
        anneeScolaireRepository.deleteById(id);
    }

    private AnneeScolaire requireAnnee(Long id) {
        return anneeScolaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Année scolaire introuvable : " + id));
    }

    private void applyRequest(AnneeScolaire annee, AnneeScolaireRequest request) {
        annee.setNom(request.nom().trim());
        annee.setDateDebut(request.dateDebut());
        annee.setDateFin(request.dateFin());
        annee.setActive(request.active());
    }

    private void validateDates(AnneeScolaireRequest request) {
        if (request.dateFin().isBefore(request.dateDebut())) {
            throw new BadRequestException("La date de fin doit être postérieure à la date de début.");
        }
    }

    private void deactivateOthersForEcole(Long ecoleId, Long excludeId) {
        anneeScolaireRepository.findByEcole_IdAndActiveTrue(ecoleId).stream()
                .filter(a -> excludeId == null || !a.getId().equals(excludeId))
                .forEach(a -> {
                    a.setActive(false);
                    anneeScolaireRepository.save(a);
                });
    }
}
