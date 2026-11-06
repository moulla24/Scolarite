package mr.ecoli.classe.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mr.ecoli.classe.dtos.ClasseDto;
import mr.ecoli.classe.dtos.ClasseRequest;
import mr.ecoli.classe.services.ClasseService;
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
@RequestMapping("/classes")
@Tag(name = "Classes", description = "CRUD des classes")
public class ClasseController {

    private final ClasseService classeService;

    public ClasseController(ClasseService classeService) {
        this.classeService = classeService;
    }

    @GetMapping
    @Operation(summary = "Liste des classes")
    public List<ClasseDto> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long niveauId) {
        return classeService.listClasses(search, niveauId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une classe")
    public ClasseDto getById(@PathVariable Long id) {
        return classeService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Créer une classe")
    public ResponseEntity<ClasseDto> create(@Valid @RequestBody ClasseRequest request) {
        ClasseDto created = classeService.create(request);
        return ResponseEntity.created(URI.create("/api/classes/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une classe")
    public ClasseDto update(@PathVariable Long id, @Valid @RequestBody ClasseRequest request) {
        return classeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une classe")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        classeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
