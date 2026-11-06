package mr.ecoli.maquette.dtos;

import java.time.LocalDate;

public record MaquetteDto(
        Long id,
        String titre,
        String matiere,
        String niveau,
        String description,
        String format,
        int nbPages,
        LocalDate dateMaj,
        String fichierUrl
) {}
