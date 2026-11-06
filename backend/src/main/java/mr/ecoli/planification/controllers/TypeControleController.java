package mr.ecoli.planification.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mr.ecoli.planification.dtos.TypeControleDto;
import mr.ecoli.planification.dtos.TypeControleRequest;
import mr.ecoli.planification.services.TypeControleService;
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
@RequestMapping("/planification/types-controles")
@Tag(name = "Planification — Types de contrôles", description = "Référentiel des types d'évaluation")
public class TypeControleController {

    private final TypeControleService typeControleService;

    public TypeControleController(TypeControleService typeControleService) {
        this.typeControleService = typeControleService;
    }

    @GetMapping
    @Operation(summary = "Liste des types de contrôles")
    public List<TypeControleDto> listTypesControles(@RequestParam(required = false) Long niveauId) {
        return typeControleService.listTypesControles(niveauId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un type de contrôle")
    public TypeControleDto getTypeControle(@PathVariable Long id) {
        return typeControleService.getTypeControle(id);
    }

    @PostMapping
    @Operation(summary = "Créer un type de contrôle")
    public ResponseEntity<TypeControleDto> createTypeControle(@Valid @RequestBody TypeControleRequest request) {
        TypeControleDto created = typeControleService.createTypeControle(request);
        return ResponseEntity.created(URI.create("/api/planification/types-controles/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un type de contrôle")
    public TypeControleDto updateTypeControle(@PathVariable Long id, @Valid @RequestBody TypeControleRequest request) {
        return typeControleService.updateTypeControle(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un type de contrôle")
    public ResponseEntity<Void> deleteTypeControle(@PathVariable Long id) {
        typeControleService.deleteTypeControle(id);
        return ResponseEntity.noContent().build();
    }
}
