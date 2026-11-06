package mr.ecoli.maquette.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mr.ecoli.common.FileStorageService;
import mr.ecoli.maquette.dtos.MaquetteAddMatiereRequest;
import mr.ecoli.maquette.dtos.MaquetteDto;
import mr.ecoli.maquette.dtos.MaquetteGrilleDto;
import mr.ecoli.maquette.dtos.MaquetteMatiereDetailDto;
import mr.ecoli.maquette.dtos.MaquetteMatiereSaveRequest;
import mr.ecoli.maquette.dtos.MaquetteRequest;
import mr.ecoli.maquette.services.MaquetteGrilleService;
import mr.ecoli.maquette.services.MaquetteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/maquettes")
@Tag(name = "Maquettes pédagogiques", description = "CRUD des maquettes pédagogiques")
public class MaquetteController {

    private final MaquetteService maquetteService;
    private final MaquetteGrilleService grilleService;
    private final FileStorageService fileStorageService;

    public MaquetteController(
            MaquetteService maquetteService,
            MaquetteGrilleService grilleService,
            FileStorageService fileStorageService) {
        this.maquetteService = maquetteService;
        this.grilleService = grilleService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/grille")
    @Operation(summary = "Maquette pédagogique par classe")
    public MaquetteGrilleDto getGrille(@RequestParam Long classeId) {
        return grilleService.getGrille(classeId);
    }

    @GetMapping("/grille/matieres/{id}")
    @Operation(summary = "Détail chapitres d'une matière")
    public MaquetteMatiereDetailDto getMatiereDetail(@PathVariable Long id) {
        return grilleService.getMatiereDetail(id);
    }

    @PutMapping("/grille/matieres/{id}")
    @Operation(summary = "Enregistrer volume horaire et chapitres")
    public MaquetteMatiereDetailDto saveMatiere(
            @PathVariable Long id, @Valid @RequestBody MaquetteMatiereSaveRequest request) {
        return grilleService.saveMatiere(id, request);
    }

    @PostMapping("/grille/matieres")
    @Operation(summary = "Ajouter une matière à la maquette")
    public MaquetteGrilleDto addMatiere(
            @RequestParam Long classeId, @Valid @RequestBody MaquetteAddMatiereRequest request) {
        return grilleService.addMatiere(classeId, request);
    }

    @DeleteMapping("/grille/matieres/{id}")
    @Operation(summary = "Retirer une matière de la maquette")
    public MaquetteGrilleDto removeMatiere(@PathVariable Long id) {
        return grilleService.removeMatiere(id);
    }

    @GetMapping("/grille/template")
    @Operation(summary = "Télécharger le modèle Excel de maquette")
    public ResponseEntity<byte[]> downloadGrilleTemplate() {
        byte[] excel = grilleService.templateExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=modele-maquette.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/grille/export")
    @Operation(summary = "Exporter la maquette en Excel")
    public ResponseEntity<byte[]> exportGrille(@RequestParam Long classeId) {
        byte[] excel = grilleService.exportExcel(classeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=maquette-classe.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @PostMapping(value = "/grille/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importer une maquette depuis Excel")
    public MaquetteGrilleDto importGrille(
            @RequestParam Long classeId, @RequestParam("file") MultipartFile file) throws java.io.IOException {
        return grilleService.importExcel(classeId, file.getBytes());
    }

    @GetMapping
    @Operation(summary = "Liste des maquettes")
    public List<MaquetteDto> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long matiereId,
            @RequestParam(required = false) Long niveauId) {
        return maquetteService.listMaquettes(search, matiereId, niveauId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une maquette")
    public MaquetteDto getById(@PathVariable Long id) {
        return maquetteService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Créer une maquette")
    public ResponseEntity<MaquetteDto> create(@Valid @RequestBody MaquetteRequest request) {
        MaquetteDto created = maquetteService.create(request);
        return ResponseEntity.created(URI.create("/api/maquettes/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une maquette")
    public MaquetteDto update(@PathVariable Long id, @Valid @RequestBody MaquetteRequest request) {
        return maquetteService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une maquette")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        maquetteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Uploader un fichier de maquette")
    public java.util.Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        return java.util.Map.of("url", fileStorageService.store(file));
    }
}
