package mr.ecoli.maquette.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.Classe;
import mr.ecoli.dao.entities.Enseignement;
import mr.ecoli.dao.entities.MaquetteChapitre;
import mr.ecoli.dao.entities.MaquetteClasse;
import mr.ecoli.dao.entities.MaquetteMatiere;
import mr.ecoli.dao.entities.Matiere;
import mr.ecoli.dao.repositories.EnseignementRepository;
import mr.ecoli.dao.repositories.MaquetteChapitreRepository;
import mr.ecoli.dao.repositories.MaquetteClasseRepository;
import mr.ecoli.dao.repositories.MaquetteMatiereRepository;
import mr.ecoli.dao.repositories.MatiereRepository;
import mr.ecoli.maquette.dtos.MaquetteAddMatiereRequest;
import mr.ecoli.maquette.dtos.MaquetteChapitreDto;
import mr.ecoli.maquette.dtos.MaquetteChapitreRequest;
import mr.ecoli.maquette.dtos.MaquetteGrilleDto;
import mr.ecoli.maquette.dtos.MaquetteMatiereCardDto;
import mr.ecoli.maquette.dtos.MaquetteMatiereDetailDto;
import mr.ecoli.maquette.dtos.MaquetteMatiereSaveRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MaquetteGrilleService {

    private static final BigDecimal DEFAULT_VOLUME = BigDecimal.valueOf(3);

    private final MaquetteClasseRepository maquetteClasseRepository;
    private final MaquetteMatiereRepository maquetteMatiereRepository;
    private final MaquetteChapitreRepository chapitreRepository;
    private final EnseignementRepository enseignementRepository;
    private final MatiereRepository matiereRepository;
    private final EntityReferenceService refs;
    private final MaquetteGrilleExportService exportService;

    public MaquetteGrilleService(
            MaquetteClasseRepository maquetteClasseRepository,
            MaquetteMatiereRepository maquetteMatiereRepository,
            MaquetteChapitreRepository chapitreRepository,
            EnseignementRepository enseignementRepository,
            MatiereRepository matiereRepository,
            EntityReferenceService refs,
            MaquetteGrilleExportService exportService) {
        this.maquetteClasseRepository = maquetteClasseRepository;
        this.maquetteMatiereRepository = maquetteMatiereRepository;
        this.chapitreRepository = chapitreRepository;
        this.enseignementRepository = enseignementRepository;
        this.matiereRepository = matiereRepository;
        this.refs = refs;
        this.exportService = exportService;
    }

    public MaquetteGrilleDto getGrille(Long classeId) {
        MaquetteClasse maquette = ensureMaquette(classeId);
        return toGrille(maquette);
    }

    @Transactional(readOnly = true)
    public MaquetteMatiereDetailDto getMatiereDetail(Long maquetteMatiereId) {
        MaquetteMatiere mm = maquetteMatiereRepository
                .findByIdWithDetails(maquetteMatiereId)
                .orElseThrow(() -> new BadRequestException("Matière de maquette introuvable."));
        List<MaquetteChapitreDto> chapitres = chapitreRepository.findByMaquetteMatiereId(mm.getId()).stream()
                .map(this::toChapitreDto)
                .toList();
        Classe classe = mm.getMaquetteClasse().getClasse();
        return new MaquetteMatiereDetailDto(
                mm.getId(),
                mm.getMatiere().getId(),
                mm.getMatiere().getNom(),
                mm.getVolumeHoraire(),
                classe.getLibelleComplet(),
                classe.getNiveau().getNom(),
                chapitres);
    }

    public MaquetteMatiereDetailDto saveMatiere(Long maquetteMatiereId, MaquetteMatiereSaveRequest request) {
        MaquetteMatiere mm = maquetteMatiereRepository
                .findByIdWithDetails(maquetteMatiereId)
                .orElseThrow(() -> new BadRequestException("Matière de maquette introuvable."));
        mm.setVolumeHoraire(request.volumeHoraire());
        maquetteMatiereRepository.save(mm);
        replaceChapitres(mm, request.chapitres());
        return getMatiereDetail(maquetteMatiereId);
    }

    public MaquetteGrilleDto addMatiere(Long classeId, MaquetteAddMatiereRequest request) {
        String nom = request.nom().trim();
        if (nom.isBlank()) {
            throw new BadRequestException("Le nom de la matière est obligatoire.");
        }
        if (request.volumeHoraire().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le volume horaire doit être supérieur à 0.");
        }

        MaquetteClasse maquette = ensureMaquette(classeId);
        Matiere matiere = findOrCreateMatiere(nom);

        if (maquetteMatiereRepository.existsByMaquetteClasse_IdAndMatiere_Id(maquette.getId(), matiere.getId())) {
            throw new BadRequestException("Cette matière est déjà dans la maquette.");
        }

        MaquetteMatiere mm = new MaquetteMatiere();
        mm.setMaquetteClasse(maquette);
        mm.setMatiere(matiere);
        mm.setVolumeHoraire(request.volumeHoraire());
        maquetteMatiereRepository.save(mm);

        List<MaquetteChapitreRequest> chapitres = request.chapitres() != null ? request.chapitres() : List.of();
        if (chapitres.isEmpty()) {
            seedDefaultChapitres(mm, matiere.getNom());
        } else {
            replaceChapitres(mm, chapitres);
        }
        return toGrille(maquette);
    }

    public MaquetteGrilleDto removeMatiere(Long maquetteMatiereId) {
        MaquetteMatiere mm = maquetteMatiereRepository
                .findByIdWithDetails(maquetteMatiereId)
                .orElseThrow(() -> new BadRequestException("Matière de maquette introuvable."));
        Long classeId = mm.getMaquetteClasse().getClasse().getId();
        chapitreRepository.deleteByMaquetteMatiere_Id(mm.getId());
        maquetteMatiereRepository.delete(mm);
        return getGrille(classeId);
    }

    public byte[] templateExcel() {
        return exportService.templateExcel();
    }

    public byte[] exportExcel(Long classeId) {
        MaquetteGrilleDto grille = getGrille(classeId);
        List<MaquetteMatiereDetailDto> details = grille.matieres().stream()
                .map(m -> getMatiereDetail(m.id()))
                .toList();
        return exportService.toExcel(grille, details);
    }

    public MaquetteGrilleDto importExcel(Long classeId, byte[] content) {
        MaquetteClasse maquette = ensureMaquette(classeId);
        exportService.importExcel(maquette, content, this);
        return toGrille(maquette);
    }

    MaquetteMatiere findOrCreateMatiereLine(MaquetteClasse maquette, String matiereNom, BigDecimal volume) {
        Map<String, MaquetteMatiere> byNom = maquetteMatiereRepository.findByMaquetteClasseId(maquette.getId()).stream()
                .collect(Collectors.toMap(m -> m.getMatiere().getNom().toLowerCase(), m -> m, (a, b) -> a));
        MaquetteMatiere existing = byNom.get(matiereNom.trim().toLowerCase());
        if (existing != null) {
            existing.setVolumeHoraire(volume != null ? volume : DEFAULT_VOLUME);
            return maquetteMatiereRepository.save(existing);
        }
        Matiere matiere = findOrCreateMatiere(matiereNom);
        MaquetteMatiere mm = new MaquetteMatiere();
        mm.setMaquetteClasse(maquette);
        mm.setMatiere(matiere);
        mm.setVolumeHoraire(volume != null ? volume : DEFAULT_VOLUME);
        return maquetteMatiereRepository.save(mm);
    }

    void replaceChapitresForImport(MaquetteMatiere mm, List<MaquetteChapitreRequest> chapitres) {
        replaceChapitres(mm, chapitres);
    }

    private MaquetteClasse ensureMaquette(Long classeId) {
        return maquetteClasseRepository.findByClasseId(classeId).orElseGet(() -> {
            createFromEnseignements(refs.requireClasse(classeId));
            return maquetteClasseRepository.findByClasseId(classeId).orElseThrow();
        });
    }

    private MaquetteClasse createFromEnseignements(Classe classe) {
        MaquetteClasse mc = new MaquetteClasse();
        mc.setClasse(classe);
        mc = maquetteClasseRepository.save(mc);

        List<Enseignement> enseignements = enseignementRepository.findByClasseId(classe.getId());
        for (Enseignement en : enseignements) {
            if (maquetteMatiereRepository.existsByMaquetteClasse_IdAndMatiere_Id(mc.getId(), en.getMatiere().getId())) {
                continue;
            }
            MaquetteMatiere mm = new MaquetteMatiere();
            mm.setMaquetteClasse(mc);
            mm.setMatiere(en.getMatiere());
            mm.setVolumeHoraire(defaultVolumeFor(en.getMatiere().getNom()));
            maquetteMatiereRepository.save(mm);
            seedDefaultChapitres(mm, en.getMatiere().getNom());
        }
        return mc;
    }

    private void seedDefaultChapitres(MaquetteMatiere mm, String matiereNom) {
        String[] defaults = defaultChapitresFor(matiereNom);
        short ordre = 1;
        for (String titre : defaults) {
            MaquetteChapitre ch = new MaquetteChapitre();
            ch.setMaquetteMatiere(mm);
            ch.setOrdre(ordre++);
            ch.setTitre(titre);
            ch.setDescription(null);
            chapitreRepository.save(ch);
        }
    }

    private BigDecimal defaultVolumeFor(String matiere) {
        String n = matiere.toLowerCase();
        if (n.contains("math") || n.contains("fran") || n.contains("arabe")) {
            return BigDecimal.valueOf(5);
        }
        if (n.contains("sport") || n.contains("art")) {
            return BigDecimal.valueOf(2);
        }
        return DEFAULT_VOLUME;
    }

    private String[] defaultChapitresFor(String matiere) {
        return new String[] {
            "Chapitre 1 — Introduction",
            "Chapitre 2 — Approfondissement",
            "Chapitre 3 — Révision"
        };
    }

    private void replaceChapitres(MaquetteMatiere mm, List<MaquetteChapitreRequest> chapitres) {
        chapitreRepository.deleteByMaquetteMatiere_Id(mm.getId());
        short ordre = 1;
        for (MaquetteChapitreRequest req : chapitres) {
            if (req.titre() == null || req.titre().isBlank()) {
                continue;
            }
            MaquetteChapitre ch = new MaquetteChapitre();
            ch.setMaquetteMatiere(mm);
            ch.setOrdre(req.ordre() != null ? req.ordre() : ordre++);
            ch.setTitre(req.titre().trim());
            ch.setDescription(req.description());
            chapitreRepository.save(ch);
        }
    }

    private MaquetteGrilleDto toGrille(MaquetteClasse maquette) {
        Classe classe = maquette.getClasse();
        List<MaquetteMatiereCardDto> cards = maquetteMatiereRepository.findByMaquetteClasseId(maquette.getId()).stream()
                .map(mm -> new MaquetteMatiereCardDto(
                        mm.getId(),
                        mm.getMatiere().getId(),
                        mm.getMatiere().getNom(),
                        mm.getVolumeHoraire(),
                        chapitreRepository.findByMaquetteMatiereId(mm.getId()).size()))
                .toList();
        return new MaquetteGrilleDto(
                maquette.getId(),
                classe.getId(),
                classe.getLibelleComplet(),
                classe.getNiveau().getNom(),
                cards);
    }

    private MaquetteChapitreDto toChapitreDto(MaquetteChapitre ch) {
        return new MaquetteChapitreDto(ch.getId(), ch.getOrdre(), ch.getTitre(), ch.getDescription());
    }

    private Matiere findOrCreateMatiere(String nom) {
        return matiereRepository
                .findByNomIgnoreCase(nom)
                .orElseGet(() -> {
                    Matiere matiere = new Matiere();
                    matiere.setNom(nom);
                    matiere.setCode(uniqueMatiereCode(nom));
                    matiere.setCoefficient(BigDecimal.ONE);
                    return matiereRepository.save(matiere);
                });
    }

    private String uniqueMatiereCode(String nom) {
        String base = nom.replaceAll("[^A-Za-zÀ-ÿ0-9]", "").toUpperCase();
        if (base.length() > 12) {
            base = base.substring(0, 12);
        }
        if (base.isBlank()) {
            base = "MAT";
        }
        String code = base;
        int suffix = 1;
        while (matiereRepository.findByCodeIgnoreCase(code).isPresent()) {
            String candidate = base + suffix++;
            code = candidate.length() > 20 ? candidate.substring(0, 20) : candidate;
        }
        return code;
    }
}
