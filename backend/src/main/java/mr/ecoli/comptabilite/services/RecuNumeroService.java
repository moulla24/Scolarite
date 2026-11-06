package mr.ecoli.comptabilite.services;

import java.time.Year;
import mr.ecoli.dao.entities.CompteurRecu;
import mr.ecoli.dao.repositories.CompteurRecuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecuNumeroService {

    private final CompteurRecuRepository compteurRecuRepository;

    public RecuNumeroService(CompteurRecuRepository compteurRecuRepository) {
        this.compteurRecuRepository = compteurRecuRepository;
    }

    @Transactional
    public String genererNumeroRecu() {
        int annee = Year.now().getValue();
        CompteurRecu compteur = compteurRecuRepository
                .findForUpdate(annee)
                .orElseGet(() -> {
                    CompteurRecu c = new CompteurRecu();
                    c.setAnnee(annee);
                    c.setDernierNumero(0);
                    return compteurRecuRepository.save(c);
                });
        int next = compteur.getDernierNumero() + 1;
        compteur.setDernierNumero(next);
        compteurRecuRepository.save(compteur);
        return annee + "-" + String.format("%05d", next);
    }
}
