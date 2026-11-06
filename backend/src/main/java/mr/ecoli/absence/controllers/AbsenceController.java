package mr.ecoli.absence.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import mr.ecoli.absence.dtos.FeuillePresenceDto;
import mr.ecoli.absence.dtos.FeuillePresenceSaveRequest;
import mr.ecoli.absence.services.AbsenceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/absences")
@Tag(name = "Absences", description = "Saisie des présences et absences par classe et plage horaire")
public class AbsenceController {

    private final AbsenceService absenceService;

    public AbsenceController(AbsenceService absenceService) {
        this.absenceService = absenceService;
    }

    @GetMapping("/feuille")
    @Operation(summary = "Feuille de présence pour une classe et une date")
    public FeuillePresenceDto getFeuille(
            @RequestParam Long classeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return absenceService.getFeuille(classeId, date);
    }

    @PostMapping("/feuille")
    @Operation(summary = "Enregistrer les présences / absences")
    public FeuillePresenceDto saveFeuille(@Valid @RequestBody FeuillePresenceSaveRequest request) {
        return absenceService.saveFeuille(request);
    }
}
