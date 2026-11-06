package mr.ecoli.planification.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.AnneeScolaire;
import mr.ecoli.dao.entities.Trimestre;
import mr.ecoli.dao.entities.TrimestreTypeControle;
import mr.ecoli.dao.entities.TypeControle;
import mr.ecoli.dao.repositories.TrimestreRepository;
import mr.ecoli.dao.repositories.TrimestreTypeControleRepository;
import mr.ecoli.dao.repositories.TypeControleRepository;
import mr.ecoli.note.NoteConstants;
import mr.ecoli.note.TrimestreHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlanificationContextService {

    public record TrimestreCourant(int numero, String libelle, Long trimestreId) {}

    public record EvaluationPlan(String typeLabel, BigDecimal noteMax, String typeLibelle, int index, int quota) {}

    private final TrimestreRepository trimestreRepository;
    private final TrimestreTypeControleRepository quotaRepository;
    private final TypeControleRepository typeControleRepository;
    private final TrimestreHelper trimestreHelper;

    public PlanificationContextService(
            TrimestreRepository trimestreRepository,
            TrimestreTypeControleRepository quotaRepository,
            TypeControleRepository typeControleRepository,
            TrimestreHelper trimestreHelper) {
        this.trimestreRepository = trimestreRepository;
        this.quotaRepository = quotaRepository;
        this.typeControleRepository = typeControleRepository;
        this.trimestreHelper = trimestreHelper;
    }

    public TrimestreCourant resolveTrimestreCourant(AnneeScolaire annee) {
        LocalDate today = LocalDate.now();
        Optional<Trimestre> configured = trimestreRepository.findAllFiltered(annee.getId()).stream()
                .filter(t -> t.isActif()
                        && !today.isBefore(t.getDateDebut())
                        && !today.isAfter(t.getDateFin()))
                .min(Comparator.comparing(Trimestre::getNumero));

        if (configured.isPresent()) {
            Trimestre t = configured.get();
            return new TrimestreCourant(t.getNumero(), t.getNom(), t.getId());
        }

        int numero = trimestreHelper.trimestreCourant(annee);
        return trimestreRepository
                .findByAnneeScolaireIdAndNumero(annee.getId(), (short) numero)
                .map(t -> new TrimestreCourant(t.getNumero(), t.getNom(), t.getId()))
                .orElseGet(() -> new TrimestreCourant(numero, trimestreHelper.libelle(numero), null));
    }

    public List<EvaluationPlan> evaluationPlans(AnneeScolaire annee, Long niveauId, int trimestreNumero) {
        Optional<Trimestre> trimestre = trimestreRepository.findByAnneeScolaireIdAndNumero(
                annee.getId(), (short) trimestreNumero);

        if (trimestre.isPresent()) {
            List<TrimestreTypeControle> quotas =
                    quotaRepository.findByTrimestreAndNiveau(trimestre.get().getId(), niveauId);
            if (!quotas.isEmpty()) {
                List<EvaluationPlan> plans = new ArrayList<>();
                for (TrimestreTypeControle quota : quotas) {
                    TypeControle type = quota.getTypeControle();
                    if (!type.isActif() || quota.getNombrePrevu() <= 0) {
                        continue;
                    }
                    plans.addAll(expandType(type, quota.getNombrePrevu()));
                }
                if (!plans.isEmpty()) {
                    return plans;
                }
            }
        }

        return defaultPlans();
    }

    private List<EvaluationPlan> expandType(TypeControle type, short quota) {
        List<EvaluationPlan> plans = new ArrayList<>();
        for (int i = 1; i <= quota; i++) {
            String label = quota <= 1 ? type.getLibelle() : type.getLibelle() + " " + i;
            plans.add(new EvaluationPlan(label, type.getNoteMax(), type.getLibelle(), i, quota));
        }
        return plans;
    }

    private List<EvaluationPlan> defaultPlans() {
        return List.of(
                new EvaluationPlan(
                        NoteConstants.TYPE_DEVOIR,
                        BigDecimal.valueOf(20),
                        NoteConstants.TYPE_DEVOIR,
                        1,
                        1),
                new EvaluationPlan(
                        NoteConstants.TYPE_COMPOSITION,
                        BigDecimal.valueOf(20),
                        NoteConstants.TYPE_COMPOSITION,
                        1,
                        1));
    }
}
