package mr.ecoli.common;

import java.math.BigDecimal;
import mr.ecoli.dao.repositories.FactureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BillingBalanceService {

    private final FactureRepository factureRepository;

    public BillingBalanceService(FactureRepository factureRepository) {
        this.factureRepository = factureRepository;
    }

    public BigDecimal getSoldeEleve(Long eleveId) {
        return factureRepository.sumResteAPayerByEleveId(eleveId);
    }

    public String getStatutEleve(Long eleveId) {
        return getSoldeEleve(eleveId).compareTo(BigDecimal.ZERO) > 0 ? "EN_RETARD" : "A_JOUR";
    }

    public String getStatutTuteur(Long tuteurId) {
        BigDecimal solde = getSoldeTuteur(tuteurId);
        return solde.compareTo(BigDecimal.ZERO) > 0 ? "EN_RETARD" : "A_JOUR";
    }

    public BigDecimal getSoldeTuteur(Long tuteurId) {
        return factureRepository.sumResteAPayerByTuteurId(tuteurId);
    }
}
