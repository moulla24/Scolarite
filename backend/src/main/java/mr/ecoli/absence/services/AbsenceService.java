package mr.ecoli.absence.services;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import mr.ecoli.absence.AbsencePlages;
import mr.ecoli.absence.PresenceStatut;
import mr.ecoli.absence.dtos.FeuillePresenceDto;
import mr.ecoli.absence.dtos.FeuillePresenceSaveRequest;
import mr.ecoli.absence.dtos.PlagePresenceDto;
import mr.ecoli.absence.dtos.PresenceEleveLigneDto;
import mr.ecoli.absence.dtos.PresenceLigneSaveRequest;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.Classe;
import mr.ecoli.dao.entities.Eleve;
import mr.ecoli.dao.entities.Inscription;
import mr.ecoli.dao.entities.PresenceEleve;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.dao.repositories.PresenceEleveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AbsenceService {

    private final InscriptionRepository inscriptionRepository;
    private final PresenceEleveRepository presenceRepository;
    private final EntityReferenceService refs;

    public AbsenceService(
            InscriptionRepository inscriptionRepository,
            PresenceEleveRepository presenceRepository,
            EntityReferenceService refs) {
        this.inscriptionRepository = inscriptionRepository;
        this.presenceRepository = presenceRepository;
        this.refs = refs;
    }

    @Transactional(readOnly = true)
    public FeuillePresenceDto getFeuille(Long classeId, LocalDate date) {
        Classe classe = refs.requireClasse(classeId);
        int jourSemaine = date.getDayOfWeek().getValue();
        String jourLibelle = date.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, Locale.FRENCH);

        List<Eleve> eleves = inscriptionRepository.findActiveByClasseId(classeId).stream()
                .map(Inscription::getEleve)
                .toList();

        Map<String, PresenceEleve> absencesByKey = presenceRepository
                .findByClasseIdAndDate(classeId, date)
                .stream()
                .collect(Collectors.toMap(
                        p -> key(p.getPlage(), p.getEleve().getId()),
                        p -> p,
                        (a, b) -> a));

        List<PlagePresenceDto> plages = AbsencePlages.ALL.stream()
                .map(plage -> toPlage(plage, eleves, absencesByKey))
                .toList();

        return new FeuillePresenceDto(
                date,
                classeId,
                classe.getLibelleComplet(),
                jourSemaine,
                jourLibelle,
                true,
                plages);
    }

    public FeuillePresenceDto saveFeuille(FeuillePresenceSaveRequest request) {
        Classe classe = refs.requireClasse(request.classeId());
        List<PresenceLigneSaveRequest> lignes =
                request.lignes() != null ? request.lignes() : List.of();

        for (PresenceLigneSaveRequest ligne : lignes) {
            validatePlage(ligne.plage());
            refs.requireEleve(ligne.eleveId());

            String statut = normalizeStatut(ligne.statut());
            if (PresenceStatut.PRESENT.equals(statut)) {
                presenceRepository.deleteByClasse_IdAndEleve_IdAndDateSeanceAndPlage(
                        request.classeId(), ligne.eleveId(), request.date(), ligne.plage());
                continue;
            }

            PresenceEleve presence = presenceRepository
                    .findByClasse_IdAndEleve_IdAndDateSeanceAndPlage(
                            request.classeId(), ligne.eleveId(), request.date(), ligne.plage())
                    .orElseGet(PresenceEleve::new);

            presence.setClasse(classe);
            presence.setSeance(null);
            presence.setPlage(ligne.plage());
            presence.setEleve(refs.requireEleve(ligne.eleveId()));
            presence.setDateSeance(request.date());
            presence.setStatut(PresenceStatut.ABSENT);
            presence.setMotif(ligne.motif());
            presenceRepository.save(presence);
        }

        return getFeuille(request.classeId(), request.date());
    }

    private PlagePresenceDto toPlage(String plage, List<Eleve> eleves, Map<String, PresenceEleve> absencesByKey) {
        List<PresenceEleveLigneDto> lignes = new ArrayList<>();

        for (Eleve eleve : eleves) {
            PresenceEleve absence = absencesByKey.get(key(plage, eleve.getId()));
            if (absence != null) {
                lignes.add(new PresenceEleveLigneDto(
                        eleve.getId(),
                        eleve.getNomComplet(),
                        PresenceStatut.ABSENT,
                        absence.getMotif()));
            } else {
                lignes.add(new PresenceEleveLigneDto(
                        eleve.getId(), eleve.getNomComplet(), PresenceStatut.PRESENT, null));
            }
        }

        return new PlagePresenceDto(plage, plage, lignes);
    }

    private static void validatePlage(String plage) {
        if (plage == null || !AbsencePlages.ALL.contains(plage)) {
            throw new IllegalArgumentException("Plage invalide : " + plage);
        }
    }

    private static String key(String plage, Long eleveId) {
        return plage + ":" + eleveId;
    }

    private static String normalizeStatut(String statut) {
        if (statut == null) {
            return PresenceStatut.PRESENT;
        }
        String normalized = statut.trim().toUpperCase(Locale.ROOT);
        if (PresenceStatut.ABSENT.equals(normalized)) {
            return PresenceStatut.ABSENT;
        }
        return PresenceStatut.PRESENT;
    }
}
