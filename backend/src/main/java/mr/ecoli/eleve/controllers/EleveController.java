package mr.ecoli.eleve.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mr.ecoli.eleve.dtos.EleveDto;
import mr.ecoli.eleve.dtos.EleveRequest;
import mr.ecoli.eleve.services.EleveService;
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
@RequestMapping("/eleves")
@Tag(name = "Élèves", description = "CRUD des élèves inscrits")
public class EleveController {

    private final EleveService eleveService;

    public EleveController(EleveService eleveService) {
        this.eleveService = eleveService;
    }

    @GetMapping
    @Operation(summary = "Liste des élèves")
    public List<EleveDto> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long niveauId,
            @RequestParam(required = false) Long classeId) {
        return eleveService.listEleves(search, niveauId, classeId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un élève")
    public EleveDto getById(@PathVariable Long id) {
        return eleveService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Créer un élève")
    public ResponseEntity<EleveDto> create(@Valid @RequestBody EleveRequest request) {
        EleveDto created = eleveService.create(request);
        return ResponseEntity.created(URI.create("/api/eleves/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un élève")
    public EleveDto update(@PathVariable Long id, @Valid @RequestBody EleveRequest request) {
        return eleveService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un élève")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eleveService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
