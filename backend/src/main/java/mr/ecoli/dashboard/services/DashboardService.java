package mr.ecoli.dashboard.services;

import java.util.List;
import mr.ecoli.dashboard.dtos.DashboardActivityDto;
import mr.ecoli.dashboard.dtos.DashboardStatsDto;
import mr.ecoli.dao.repositories.ClasseRepository;
import mr.ecoli.dao.repositories.EnseignantRepository;
import mr.ecoli.dao.repositories.EnseignementRepository;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.dao.repositories.SeancePresenceRepository;
import mr.ecoli.facturation.services.PaiementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final InscriptionRepository inscriptionRepository;
    private final EnseignantRepository enseignantRepository;
    private final EnseignementRepository enseignementRepository;
    private final ClasseRepository classeRepository;
    private final SeancePresenceRepository seancePresenceRepository;
    private final PaiementService paiementService;

    public DashboardService(
            InscriptionRepository inscriptionRepository,
            EnseignantRepository enseignantRepository,
            EnseignementRepository enseignementRepository,
            ClasseRepository classeRepository,
            SeancePresenceRepository seancePresenceRepository,
            PaiementService paiementService) {
        this.inscriptionRepository = inscriptionRepository;
        this.enseignantRepository = enseignantRepository;
        this.enseignementRepository = enseignementRepository;
        this.classeRepository = classeRepository;
        this.seancePresenceRepository = seancePresenceRepository;
        this.paiementService = paiementService;
    }

    public DashboardStatsDto getStats(Long niveauId) {
        long inscrits = inscriptionRepository.countActiveInscriptions(niveauId);
        long enseignants = niveauId == null
                ? enseignantRepository.count()
                : enseignementRepository.countDistinctEnseignants(niveauId);
        long classes = classeRepository.countByNiveau(niveauId);
        long seances = seancePresenceRepository.countByNiveau(niveauId);
        return new DashboardStatsDto(inscrits, inscrits, enseignants, classes, seances);
    }

    public List<DashboardActivityDto> getRecentActivity(Long niveauId) {
        return paiementService.listRecent(10, niveauId).stream()
                .map(p -> new DashboardActivityDto(
                        p.reference() != null ? p.reference() : "PAY-" + p.id(),
                        p.parentNom(),
                        1,
                        p.niveau() != null ? p.niveau() : "—",
                        p.montant(),
                        p.datePaiement().toString(),
                        "paid"))
                .toList();
    }
}
