package mr.ecoli.planification.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mr.ecoli.planification.dtos.TrimestreDto;
import mr.ecoli.planification.dtos.TrimestreRequest;
import mr.ecoli.planification.services.TrimestreService;
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
@RequestMapping("/planification/trimestres")
@Tag(name = "Planification — Trimestres", description = "Configuration des trimestres scolaires")
public class TrimestreController {

    private final TrimestreService trimestreService;

    public TrimestreController(TrimestreService trimestreService) {
        this.trimestreService = trimestreService;
    }

    @GetMapping
    @Operation(summary = "Liste des trimestres", description = "Filtrés par année scolaire ; sans paramètre = année courante active")
    public List<TrimestreDto> listTrimestres(@RequestParam(required = false) Long anneeScolaireId) {
        return trimestreService.listTrimestres(anneeScolaireId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un trimestre")
    public TrimestreDto getTrimestre(@PathVariable Long id) {
        return trimestreService.getTrimestre(id);
    }

    @PostMapping
    @Operation(summary = "Créer un trimestre")
    public ResponseEntity<TrimestreDto> createTrimestre(@Valid @RequestBody TrimestreRequest request) {
        TrimestreDto created = trimestreService.createTrimestre(request);
        return ResponseEntity.created(URI.create("/api/planification/trimestres/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un trimestre")
    public TrimestreDto updateTrimestre(@PathVariable Long id, @Valid @RequestBody TrimestreRequest request) {
        return trimestreService.updateTrimestre(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un trimestre")
    public ResponseEntity<Void> deleteTrimestre(@PathVariable Long id) {
        trimestreService.deleteTrimestre(id);
        return ResponseEntity.noContent().build();
    }
}
