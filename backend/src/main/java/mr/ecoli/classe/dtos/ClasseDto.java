package mr.ecoli.classe.dtos;

public record ClasseDto(
        Long id,
        String nom,
        String niveau,
        String enseignantReferent,
        long effectif,
        int capacite,
        String salle,
        int tauxOccupation
) {}
