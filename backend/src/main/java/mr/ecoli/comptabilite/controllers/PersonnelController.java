package mr.ecoli.comptabilite.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import mr.ecoli.comptabilite.dtos.PaiePersonnelLigneDto;
import mr.ecoli.comptabilite.dtos.PaiementPersonnelDto;
import mr.ecoli.comptabilite.dtos.PaiementPersonnelRequest;
import mr.ecoli.comptabilite.dtos.PersonnelDto;
import mr.ecoli.comptabilite.dtos.PersonnelRequest;
import mr.ecoli.comptabilite.services.PersonnelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/comptabilite/personnel")
@Tag(name = "Personnel de service", description = "Enregistrement et paie du personnel (enseignants, agents, gardiens…)")
@PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
public class PersonnelController {

    private final PersonnelService personnelService;

    public PersonnelController(PersonnelService personnelService) {
        this.personnelService = personnelService;
    }

    @GetMapping("/categories")
    @Operation(summary = "Catégories de personnel")
    public Map<String, String> categories() {
        return personnelService.categories();
    }

    @GetMapping("/paie")
    @Operation(summary = "État de paie mensuelle")
    public List<PaiePersonnelLigneDto> paieMois(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth mois) {
        return personnelService.listPaieMois(mois);
    }

    @PostMapping("/paie")
    @Operation(summary = "Verser le salaire d'un membre")
    public ResponseEntity<PaiementPersonnelDto> payer(@Valid @RequestBody PaiementPersonnelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personnelService.payer(request));
    }

    @PostMapping("/paie/tous")
    @Operation(summary = "Verser tous les salaires du mois")
    public List<PaiementPersonnelDto> payerTous(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth mois,
            @RequestParam(defaultValue = "VIREMENT") String mode) {
        return personnelService.payerTous(mois, mode);
    }

    @GetMapping
    @Operation(summary = "Liste du personnel de service")
    public List<PersonnelDto> list(
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String search) {
        return personnelService.list(categorie, statut, search);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un membre du personnel")
    public PersonnelDto getById(@PathVariable Long id) {
        return personnelService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Enregistrer un membre du personnel")
    public ResponseEntity<PersonnelDto> create(@Valid @RequestBody PersonnelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personnelService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un membre du personnel")
    public PersonnelDto update(@PathVariable Long id, @Valid @RequestBody PersonnelRequest request) {
        return personnelService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un membre du personnel")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personnelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
