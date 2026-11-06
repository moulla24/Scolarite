package mr.ecoli.anneescolaire.controllers;

import mr.ecoli.anneescolaire.dtos.AnneeScolaireDto;
import mr.ecoli.anneescolaire.dtos.AnneeScolaireRequest;
import mr.ecoli.anneescolaire.services.AnneeScolaireService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
@RequestMapping("/annees-scolaires")
@Tag(name = "Années scolaires", description = "CRUD des années scolaires par école")
public class AnneeScolaireController {

    private final AnneeScolaireService anneeScolaireService;

    public AnneeScolaireController(AnneeScolaireService anneeScolaireService) {
        this.anneeScolaireService = anneeScolaireService;
    }

    @GetMapping
    @Operation(summary = "Liste des années scolaires")
    public List<AnneeScolaireDto> list() {
        return anneeScolaireService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une année scolaire")
    public AnneeScolaireDto getById(@PathVariable Long id) {
        return anneeScolaireService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Créer une année scolaire")
    public ResponseEntity<AnneeScolaireDto> create(@Valid @RequestBody AnneeScolaireRequest request) {
        AnneeScolaireDto created = anneeScolaireService.create(request);
        return ResponseEntity.created(URI.create("/api/annees-scolaires/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une année scolaire")
    public AnneeScolaireDto update(@PathVariable Long id, @Valid @RequestBody AnneeScolaireRequest request) {
        return anneeScolaireService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une année scolaire")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        anneeScolaireService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
