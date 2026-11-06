package mr.ecoli.tuteur.dtos;

import java.util.List;
import java.math.BigDecimal;

public record TuteurDto(
        Long id,
        String nom,
        String prenom,
        String nomComplet,
        String telephone,
        String email,
        String adresse,
        long nombreEleves,
        List<String> nomsEleves,
        List<Long> eleveIds,
        List<EleveAffecteDto> elevesAffectes,
        BigDecimal solde,
        String statutCompte
) {}
