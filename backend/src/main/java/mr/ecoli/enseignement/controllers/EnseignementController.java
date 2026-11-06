package mr.ecoli.enseignement.controllers;



import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import java.net.URI;

import java.util.List;

import mr.ecoli.enseignement.dtos.EnseignementDto;

import mr.ecoli.enseignement.dtos.EnseignementRequest;

import mr.ecoli.enseignement.services.EnseignementService;

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

@RequestMapping("/enseignements")

@Tag(name = "Enseignements", description = "Affectations classe / matière / enseignant")

public class EnseignementController {



    private final EnseignementService enseignementService;



    public EnseignementController(EnseignementService enseignementService) {

        this.enseignementService = enseignementService;

    }



    @GetMapping

    @Operation(summary = "Liste des enseignements")

    public List<EnseignementDto> list(

            @RequestParam(required = false) Long classeId,

            @RequestParam(required = false) Long matiereId) {

        return enseignementService.list(classeId, matiereId);

    }



    @GetMapping("/{id}")

    @Operation(summary = "Détail d'une affectation")

    public EnseignementDto getById(@PathVariable Long id) {

        return enseignementService.getById(id);

    }



    @PostMapping

    @Operation(summary = "Créer une affectation")

    public ResponseEntity<EnseignementDto> create(@Valid @RequestBody EnseignementRequest request) {

        EnseignementDto created = enseignementService.create(request);

        return ResponseEntity.created(URI.create("/api/enseignements/" + created.id())).body(created);

    }



    @PutMapping("/{id}")

    @Operation(summary = "Modifier une affectation")

    public EnseignementDto update(@PathVariable Long id, @Valid @RequestBody EnseignementRequest request) {

        return enseignementService.update(id, request);

    }



    @DeleteMapping("/{id}")

    @Operation(summary = "Supprimer une affectation")

    public ResponseEntity<Void> delete(@PathVariable Long id) {

        enseignementService.delete(id);

        return ResponseEntity.noContent().build();

    }

}

