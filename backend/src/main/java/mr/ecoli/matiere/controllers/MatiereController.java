package mr.ecoli.matiere.controllers;

import mr.ecoli.matiere.dtos.MatiereDto;
import mr.ecoli.matiere.services.MatiereService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matieres")
@Tag(name = "Matières", description = "Liste des matières")
public class MatiereController {

    private final MatiereService matiereService;

    public MatiereController(MatiereService matiereService) {
        this.matiereService = matiereService;
    }

    @GetMapping
    @Operation(summary = "Liste des matières")
    public List<MatiereDto> listMatieres() {
        return matiereService.listMatieres();
    }
}
