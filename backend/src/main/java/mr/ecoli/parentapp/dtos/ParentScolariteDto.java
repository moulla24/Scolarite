package mr.ecoli.parentapp.dtos;

import java.time.LocalDate;

public record ParentScolariteDto(
        Long eleveId,
        String nomComplet,
        String matricule,
        String ecoleNom,
        String ecoleCode,
        String ecoleTelephone,
        String ecoleEmail,
        String classe,
        String niveau,
        String salle,
        String anneeScolaire,
        LocalDate anneeDebut,
        LocalDate anneeFin,
        String statutInscription,
        LocalDate dateInscription
) {}
