package mr.ecoli.niveau.controllers;

import mr.ecoli.niveau.dtos.NiveauDto;
import mr.ecoli.niveau.dtos.NiveauRequest;
import mr.ecoli.niveau.services.NiveauService;

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
@RequestMapping("/niveaux")
@Tag(name = "Niveaux", description = "CRUD des niveaux scolaires")
public class NiveauController {

    private final NiveauService niveauService;

    public NiveauController(NiveauService niveauService) {
        this.niveauService = niveauService;
    }

    @GetMapping
    @Operation(summary = "Liste des niveaux")
    public List<NiveauDto> listNiveaux() {
        return niveauService.listNiveaux();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un niveau")
    public NiveauDto getNiveau(@PathVariable Long id) {
        return niveauService.getNiveau(id);
    }

    @PostMapping
    @Operation(summary = "Créer un niveau")
    public ResponseEntity<NiveauDto> createNiveau(@Valid @RequestBody NiveauRequest request) {
        NiveauDto created = niveauService.createNiveau(request);
        return ResponseEntity.created(URI.create("/api/niveaux/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un niveau")
    public NiveauDto updateNiveau(@PathVariable Long id, @Valid @RequestBody NiveauRequest request) {
        return niveauService.updateNiveau(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un niveau")
    public ResponseEntity<Void> deleteNiveau(@PathVariable Long id) {
        niveauService.deleteNiveau(id);
        return ResponseEntity.noContent().build();
    }
}
