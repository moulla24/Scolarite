package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BilanDto(
        LocalDate periodeDebut,
        LocalDate periodeFin,
        BigDecimal totalActif,
        BigDecimal totalPassif,
        BigDecimal encaissementsTotaux,
        BigDecimal creancesClients,
        BigDecimal produitsScolaires,
        BigDecimal chargesPersonnel,
        BigDecimal resultatNet,
        BigDecimal tresorerieNette,
        List<BilanLigneDto> actif,
        List<BilanLigneDto> passif,
        List<BilanLigneDto> resultat
) {}
