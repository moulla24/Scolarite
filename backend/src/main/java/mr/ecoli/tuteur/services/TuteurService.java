package mr.ecoli.tuteur.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mr.ecoli.common.BillingBalanceService;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.tuteur.dtos.TuteurDto;
import mr.ecoli.tuteur.dtos.TuteurRequest;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.Eleve;
import mr.ecoli.dao.entities.Inscription;
import mr.ecoli.dao.entities.Tuteur;
import mr.ecoli.dao.repositories.EleveRepository;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.dao.repositories.TuteurRepository;
import mr.ecoli.tuteur.dtos.EleveAffecteDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TuteurService {

    private final TuteurRepository tuteurRepository;
    private final EleveRepository eleveRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;
    private final BillingBalanceService billing;

    public TuteurService(
            TuteurRepository tuteurRepository,
            EleveRepository eleveRepository,
            InscriptionRepository inscriptionRepository,
            EntityMapper mapper,
            EntityReferenceService refs,
            BillingBalanceService billing) {
        this.tuteurRepository = tuteurRepository;
        this.eleveRepository = eleveRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.mapper = mapper;
        this.refs = refs;
        this.billing = billing;
    }
    @Transactional(readOnly = true)
    public List<TuteurDto> listParents(String search, Long niveauId, String statutCompte) {
        return tuteurRepository.search(normalize(search)).stream()
                .filter(t -> !refs.isUnassignedTuteur(t))
                .map(t -> toDto(t, niveauId))
                .filter(dto -> niveauId == null || dto.nombreEleves() > 0)
                .filter(dto -> matchesStatut(dto.statutCompte(), statutCompte))
                .toList();
    }
    @Transactional(readOnly = true)
    public TuteurDto getById(Long id) {
        return toDto(refs.requireTuteur(id), null);
    }
    public TuteurDto create(TuteurRequest request) {
        Tuteur tuteur = new Tuteur();
        apply(tuteur, request);
        tuteur = tuteurRepository.save(tuteur);
        syncEleves(tuteur, request.eleveIds());
        return toDto(tuteur, null);
    }
    public TuteurDto update(Long id, TuteurRequest request) {
        Tuteur tuteur = refs.requireTuteur(id);
        if (refs.isUnassignedTuteur(tuteur)) {
            throw new BadRequestException("Ce parent système ne peut pas être modifié.");
        }
        apply(tuteur, request);
        tuteur = tuteurRepository.save(tuteur);
        syncEleves(tuteur, request.eleveIds());
        return toDto(tuteur, null);
    }
    public void delete(Long id) {
        Tuteur tuteur = refs.requireTuteur(id);
        if (refs.isUnassignedTuteur(tuteur)) {
            throw new BadRequestException("Ce parent système ne peut pas être supprimé.");
        }
        if (eleveRepository.countByTuteurId(id) > 0) {
            throw new BadRequestException("Impossible de supprimer : des élèves sont liés à ce parent.");
        }
        tuteurRepository.delete(tuteur);
    }

    private void apply(Tuteur tuteur, TuteurRequest request) {
        tuteur.setNom(request.nom());
        tuteur.setPrenom(request.prenom());
        tuteur.setEmail(request.email());
        tuteur.setTelephone(request.telephone());
        tuteur.setAdresse(request.adresse());
    }

    private TuteurDto toDto(Tuteur tuteur, Long niveauId) {
        List<Eleve> eleves = eleveRepository.search(null, niveauId, null).stream()
                .filter(e -> e.getTuteur().getId().equals(tuteur.getId()))
                .toList();
        if (niveauId != null && eleves.isEmpty()) {
            eleves = List.of();
        } else if (niveauId == null) {
            eleves = eleveRepository.search(null, null, null).stream()
                    .filter(e -> e.getTuteur().getId().equals(tuteur.getId()))
                    .toList();
        }
        return mapper.toTuteurDto(
                tuteur,
                eleves,
                toElevesAffectes(eleves),
                billing.getSoldeTuteur(tuteur.getId()),
                billing.getStatutTuteur(tuteur.getId()));
    }

    private List<EleveAffecteDto> toElevesAffectes(List<Eleve> eleves) {
        return eleves.stream().map(eleve -> {
            Inscription inscription = inscriptionRepository.findActiveForEleve(eleve.getId()).orElse(null);
            String classe = inscription != null ? inscription.getClasse().getLibelleComplet() : "—";
            String niveau = inscription != null ? inscription.getClasse().getNiveau().getNom() : "—";
            return new EleveAffecteDto(eleve.getId(), eleve.getNomComplet(), niveau, classe);
        }).toList();
    }

    private String normalize(String search) {
        return search == null ? "" : search.trim();
    }

    private boolean matchesStatut(String dtoStatut, String filterStatut) {
        if (filterStatut == null || filterStatut.isBlank()) {
            return true;
        }
        return dtoStatut != null && dtoStatut.equalsIgnoreCase(filterStatut.trim());
    }

    private void syncEleves(Tuteur tuteur, List<Long> eleveIds) {
        if (refs.isUnassignedTuteur(tuteur)) {
            return;
        }
        Set<Long> desired = new HashSet<>(eleveIds != null ? eleveIds : List.of());
        for (Long eleveId : desired) {
            Eleve eleve = refs.requireEleve(eleveId);
            eleve.setTuteur(tuteur);
            eleveRepository.save(eleve);
        }
        Tuteur unassigned = refs.unassignedTuteur();
        for (Eleve eleve : eleveRepository.findByTuteurId(tuteur.getId())) {
            if (!desired.contains(eleve.getId())) {
                eleve.setTuteur(unassigned);
                eleveRepository.save(eleve);
            }
        }
    }
}
