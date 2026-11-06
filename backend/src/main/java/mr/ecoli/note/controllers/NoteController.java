package mr.ecoli.note.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mr.ecoli.note.dtos.BulletinDetailDto;
import mr.ecoli.note.dtos.BulletinEleveDto;
import mr.ecoli.note.dtos.BulletinEleveStatutDto;
import mr.ecoli.note.dtos.BulletinTrimestreDto;
import mr.ecoli.note.dtos.GrilleNotesDto;
import mr.ecoli.note.dtos.NotesImportLigneDto;
import mr.ecoli.note.dtos.NotesImportResultDto;
import mr.ecoli.note.dtos.NotesSummaryLigneDto;
import mr.ecoli.note.dtos.SaisieNotesDto;
import mr.ecoli.note.dtos.SaisieSaveRequest;
import mr.ecoli.note.dtos.TrimestreInfoDto;
import mr.ecoli.evaluation.dtos.EvaluationDto;
import mr.ecoli.note.dtos.NoteDto;
import mr.ecoli.evaluation.dtos.EvaluationRequest;
import mr.ecoli.note.dtos.NoteRequest;
import mr.ecoli.note.services.NoteService;
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

@RestController
@RequestMapping("/notes")
@Tag(name = "Notes", description = "CRUD des évaluations et notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/bulletins")
    @Operation(summary = "Bulletins par classe / niveau")
    public List<BulletinEleveDto> listBulletins(
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long niveauId) {
        return noteService.listBulletins(classeId, niveauId);
    }

    @GetMapping("/bulletins/{eleveId}")
    @Operation(summary = "Bulletin détaillé d'un élève")
    public BulletinDetailDto getBulletinDetail(@PathVariable Long eleveId) {
        return noteService.getBulletinDetail(eleveId);
    }

    @GetMapping("/trimestre-courant")
    @Operation(summary = "Trimestre scolaire en cours")
    public TrimestreInfoDto getTrimestreCourant() {
        return noteService.getTrimestreCourant();
    }

    @GetMapping("/saisie")
    @Operation(summary = "Grille de saisie pour le trimestre courant")
    public SaisieNotesDto getSaisie(
            @RequestParam Long classeId,
            @RequestParam Long matiereId) {
        return noteService.getSaisie(classeId, matiereId);
    }

    @PostMapping("/saisie")
    @Operation(summary = "Enregistrer les notes du trimestre courant")
    public ResponseEntity<Void> saveSaisie(@Valid @RequestBody SaisieSaveRequest request) {
        noteService.saveSaisie(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bulletins/statuts")
    @Operation(summary = "Disponibilité des bulletins par trimestre")
    public List<BulletinEleveStatutDto> listBulletinStatuts(
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long niveauId) {
        return noteService.listBulletinStatuts(classeId, niveauId);
    }

    @GetMapping("/bulletins/{eleveId}/trimestre/{trimestre}")
    @Operation(summary = "Bulletin trimestriel d'un élève")
    public BulletinTrimestreDto getBulletinTrimestre(
            @PathVariable Long eleveId,
            @PathVariable int trimestre) {
        return noteService.getBulletinTrimestre(eleveId, trimestre);
    }

    @GetMapping("/bulletins/{eleveId}/trimestre/{trimestre}/pdf")
    @Operation(summary = "Télécharger le bulletin trimestriel en PDF")
    public ResponseEntity<byte[]> exportBulletinPdf(
            @PathVariable Long eleveId,
            @PathVariable int trimestre) {
        byte[] pdf = noteService.exportBulletinPdf(eleveId, trimestre);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bulletin-T" + trimestre + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/bulletins/{eleveId}/trimestre/{trimestre}/excel")
    @Operation(summary = "Télécharger le bulletin trimestriel en Excel")
    public ResponseEntity<byte[]> exportBulletinExcel(
            @PathVariable Long eleveId,
            @PathVariable int trimestre) {
        byte[] excel = noteService.exportBulletinExcel(eleveId, trimestre);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=bulletin-T" + trimestre + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/summary")
    @Operation(summary = "Synthèse par élève (nombre de matières notées)")
    public List<NotesSummaryLigneDto> getSummary(
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long niveauId) {
        return noteService.getSummary(classeId, niveauId);
    }

    @GetMapping("/grille")
    @Operation(summary = "Grille notes par classe et matière")
    public GrilleNotesDto getGrille(
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long niveauId,
            @RequestParam(required = false) Long matiereId) {
        return noteService.getGrille(classeId, niveauId, matiereId);
    }

    @PostMapping("/import")
    @Operation(summary = "Importer des notes (matricule, matière, évaluation)")
    public NotesImportResultDto importNotes(@Valid @RequestBody List<NotesImportLigneDto> lignes) {
        return noteService.importNotes(lignes);
    }

    @GetMapping("/evaluations")
    @Operation(summary = "Liste des évaluations")
    public List<EvaluationDto> listEvaluations(@RequestParam(required = false) Long classeId) {
        return noteService.listEvaluations(classeId);
    }

    @GetMapping("/evaluations/{id}")
    @Operation(summary = "Détail d'une évaluation")
    public EvaluationDto getEvaluation(@PathVariable Long id) {
        return noteService.getEvaluation(id);
    }

    @PostMapping("/evaluations")
    @Operation(summary = "Créer une évaluation")
    public ResponseEntity<EvaluationDto> createEvaluation(@Valid @RequestBody EvaluationRequest request) {
        EvaluationDto created = noteService.createEvaluation(request);
        return ResponseEntity.created(URI.create("/api/notes/evaluations/" + created.id())).body(created);
    }

    @PutMapping("/evaluations/{id}")
    @Operation(summary = "Modifier une évaluation")
    public EvaluationDto updateEvaluation(@PathVariable Long id, @Valid @RequestBody EvaluationRequest request) {
        return noteService.updateEvaluation(id, request);
    }

    @DeleteMapping("/evaluations/{id}")
    @Operation(summary = "Supprimer une évaluation")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Long id) {
        noteService.deleteEvaluation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/evaluations/{evaluationId}/notes")
    @Operation(summary = "Notes d'une évaluation")
    public List<NoteDto> listNotes(@PathVariable Long evaluationId) {
        return noteService.listNotesByEvaluation(evaluationId);
    }

    @GetMapping("/notes/{id}")
    @Operation(summary = "Détail d'une note")
    public NoteDto getNote(@PathVariable Long id) {
        return noteService.getNote(id);
    }

    @PostMapping("/notes")
    @Operation(summary = "Saisir une note")
    public ResponseEntity<NoteDto> createNote(@Valid @RequestBody NoteRequest request) {
        NoteDto created = noteService.createNote(request);
        return ResponseEntity.created(URI.create("/api/notes/notes/" + created.id())).body(created);
    }

    @PutMapping("/notes/{id}")
    @Operation(summary = "Modifier une note")
    public NoteDto updateNote(@PathVariable Long id, @Valid @RequestBody NoteRequest request) {
        return noteService.updateNote(id, request);
    }

    @DeleteMapping("/notes/{id}")
    @Operation(summary = "Supprimer une note")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
