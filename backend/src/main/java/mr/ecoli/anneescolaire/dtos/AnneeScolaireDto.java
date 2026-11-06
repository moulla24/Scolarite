package mr.ecoli.anneescolaire.dtos;

import java.time.LocalDate;

public record AnneeScolaireDto(
        Long id,
        Long ecoleId,
        String ecoleNom,
        String nom,
        LocalDate dateDebut,
        LocalDate dateFin,
        boolean active
) {}
