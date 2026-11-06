package mr.ecoli.ecole.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mr.ecoli.ecole.dtos.EcoleDto;
import mr.ecoli.ecole.dtos.EcoleRequest;
import mr.ecoli.ecole.services.EcoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ecoles")
@Tag(name = "Écoles", description = "CRUD des établissements (référence pour niveaux, élèves, années scolaires)")
public class EcoleController {

    private final EcoleService ecoleService;

    public EcoleController(EcoleService ecoleService) {
        this.ecoleService = ecoleService;
    }

    @GetMapping
    @Operation(summary = "Liste des écoles")
    public List<EcoleDto> list() {
        return ecoleService.listEcoles();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une école")
    public EcoleDto getById(@PathVariable Long id) {
        return ecoleService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Créer une école")
    public ResponseEntity<EcoleDto> create(@Valid @RequestBody EcoleRequest request) {
        EcoleDto created = ecoleService.create(request);
        return ResponseEntity.created(URI.create("/api/ecoles/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une école")
    public EcoleDto update(@PathVariable Long id, @Valid @RequestBody EcoleRequest request) {
        return ecoleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une école")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ecoleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
