package mr.ecoli.seance.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mr.ecoli.seance.dtos.SeanceEmploiDto;
import mr.ecoli.seance.dtos.SeanceRequest;
import mr.ecoli.seance.services.EmploiDuTempsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emploi-du-temps")
@Tag(name = "Emploi du temps", description = "CRUD du planning des séances")
public class EmploiDuTempsController {

    private final EmploiDuTempsService emploiDuTempsService;

    public EmploiDuTempsController(EmploiDuTempsService emploiDuTempsService) {
        this.emploiDuTempsService = emploiDuTempsService;
    }

    @GetMapping
    @Operation(summary = "Liste des créneaux")
    public List<SeanceEmploiDto> list(
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long semaineId) {
        return emploiDuTempsService.listSeances(classeId, semaineId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un créneau")
    public SeanceEmploiDto getById(@PathVariable Long id) {
        return emploiDuTempsService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Créer un créneau")
    public ResponseEntity<SeanceEmploiDto> create(@Valid @RequestBody SeanceRequest request) {
        SeanceEmploiDto created = emploiDuTempsService.create(request);
        return ResponseEntity.created(URI.create("/api/emploi-du-temps/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un créneau")
    public SeanceEmploiDto update(@PathVariable Long id, @Valid @RequestBody SeanceRequest request) {
        return emploiDuTempsService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un créneau")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        emploiDuTempsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
