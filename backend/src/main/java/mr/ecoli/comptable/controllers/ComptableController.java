package mr.ecoli.comptable.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mr.ecoli.comptable.dtos.ComptableDto;
import mr.ecoli.comptable.dtos.ComptableRequest;
import mr.ecoli.comptable.services.ComptableService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/comptables")
@Tag(name = "Comptables", description = "Gestion des comptes comptables")
@PreAuthorize("hasRole('ADMIN')")
public class ComptableController {

    private final ComptableService comptableService;

    public ComptableController(ComptableService comptableService) {
        this.comptableService = comptableService;
    }

    @GetMapping
    @Operation(summary = "Liste des comptables")
    public List<ComptableDto> list(@RequestParam(required = false) String search) {
        return comptableService.list(search);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un comptable")
    public ComptableDto getById(@PathVariable Long id) {
        return comptableService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Créer un comptable")
    public ResponseEntity<ComptableDto> create(@Valid @RequestBody ComptableRequest request) {
        ComptableDto created = comptableService.create(request);
        return ResponseEntity.created(URI.create("/api/comptables/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un comptable")
    public ComptableDto update(@PathVariable Long id, @Valid @RequestBody ComptableRequest request) {
        return comptableService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un comptable")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        comptableService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
