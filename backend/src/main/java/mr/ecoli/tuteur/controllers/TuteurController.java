package mr.ecoli.tuteur.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mr.ecoli.tuteur.dtos.TuteurDto;
import mr.ecoli.tuteur.dtos.TuteurRequest;
import mr.ecoli.tuteur.services.TuteurService;
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
@RequestMapping("/parents")
@Tag(name = "Parents", description = "CRUD des tuteurs / parents d'élèves")
public class TuteurController {

    private final TuteurService tuteurService;

    public TuteurController(TuteurService tuteurService) {
        this.tuteurService = tuteurService;
    }

    @GetMapping
    @Operation(summary = "Liste des parents")
    public List<TuteurDto> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long niveauId,
            @RequestParam(required = false) String statutCompte) {
        return tuteurService.listParents(search, niveauId, statutCompte);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un parent")
    public TuteurDto getById(@PathVariable Long id) {
        return tuteurService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Créer un parent")
    public ResponseEntity<TuteurDto> create(@Valid @RequestBody TuteurRequest request) {
        TuteurDto created = tuteurService.create(request);
        return ResponseEntity.created(URI.create("/api/parents/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un parent")
    public TuteurDto update(@PathVariable Long id, @Valid @RequestBody TuteurRequest request) {
        return tuteurService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un parent")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tuteurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
