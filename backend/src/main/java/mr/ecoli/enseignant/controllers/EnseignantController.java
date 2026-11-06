package mr.ecoli.enseignant.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mr.ecoli.enseignant.dtos.EnseignantDto;
import mr.ecoli.enseignant.dtos.EnseignantRequest;
import mr.ecoli.enseignant.services.EnseignantService;
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
@RequestMapping("/enseignants")
@Tag(name = "Enseignants", description = "CRUD du corps enseignant")
public class EnseignantController {

    private final EnseignantService enseignantService;

    public EnseignantController(EnseignantService enseignantService) {
        this.enseignantService = enseignantService;
    }

    @GetMapping
    @Operation(summary = "Liste des enseignants")
    public List<EnseignantDto> list(@RequestParam(required = false) String search) {
        return enseignantService.listEnseignants(search);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un enseignant")
    public EnseignantDto getById(@PathVariable Long id) {
        return enseignantService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Créer un enseignant")
    public ResponseEntity<EnseignantDto> create(@Valid @RequestBody EnseignantRequest request) {
        EnseignantDto created = enseignantService.create(request);
        return ResponseEntity.created(URI.create("/api/enseignants/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un enseignant")
    public EnseignantDto update(@PathVariable Long id, @Valid @RequestBody EnseignantRequest request) {
        return enseignantService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un enseignant")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enseignantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
