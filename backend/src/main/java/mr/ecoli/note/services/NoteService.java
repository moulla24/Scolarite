package mr.ecoli.note.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.dao.entities.AnneeScolaire;
import mr.ecoli.note.NoteConstants;
import mr.ecoli.note.TrimestreHelper;
import mr.ecoli.planification.services.PlanificationContextService;
import mr.ecoli.planification.services.PlanificationContextService.EvaluationPlan;
import mr.ecoli.planification.services.PlanificationContextService.TrimestreCourant;
import mr.ecoli.note.dtos.SaisieColonneDto;
import mr.ecoli.note.dtos.SaisieNoteValueDto;
import mr.ecoli.note.dtos.BulletinDetailDto;
import mr.ecoli.note.dtos.BulletinEleveDto;
import mr.ecoli.note.dtos.BulletinEleveStatutDto;
import mr.ecoli.note.dtos.BulletinLigneDto;
import mr.ecoli.note.dtos.BulletinMatiereDto;
import mr.ecoli.note.dtos.BulletinTrimestreDto;
import mr.ecoli.note.dtos.SaisieLigneDto;
import mr.ecoli.note.dtos.SaisieNoteCellDto;
import mr.ecoli.note.dtos.SaisieNoteItemDto;
import mr.ecoli.note.dtos.SaisieNotesDto;
import mr.ecoli.note.dtos.SaisieSaveRequest;
import mr.ecoli.note.dtos.TrimestreInfoDto;
import mr.ecoli.note.dtos.GrilleColonneDto;
import mr.ecoli.note.dtos.GrilleLigneDto;
import mr.ecoli.note.dtos.GrilleNotesDto;
import mr.ecoli.note.dtos.NotesImportLigneDto;
import mr.ecoli.note.dtos.NotesImportResultDto;
import mr.ecoli.note.dtos.NotesSummaryLigneDto;
import mr.ecoli.evaluation.dtos.EvaluationDto;
import mr.ecoli.note.dtos.NoteDto;
import mr.ecoli.evaluation.dtos.EvaluationRequest;
import mr.ecoli.note.dtos.NoteRequest;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.Eleve;
import mr.ecoli.dao.entities.Enseignement;
import mr.ecoli.dao.entities.Evaluation;
import mr.ecoli.dao.entities.Inscription;
import mr.ecoli.dao.entities.Matiere;
import mr.ecoli.dao.entities.Note;
import mr.ecoli.dao.repositories.EleveRepository;
import mr.ecoli.dao.repositories.EnseignementRepository;
import mr.ecoli.dao.repositories.EvaluationRepository;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.dao.repositories.MatiereRepository;
import mr.ecoli.dao.repositories.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final EvaluationRepository evaluationRepository;
    private final EleveRepository eleveRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EnseignementRepository enseignementRepository;
    private final MatiereRepository matiereRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;
    private final TrimestreHelper trimestreHelper;
    private final PlanificationContextService planificationContext;
    private final BulletinExportService bulletinExportService;

    public NoteService(
            NoteRepository noteRepository,
            EvaluationRepository evaluationRepository,
            EleveRepository eleveRepository,
            InscriptionRepository inscriptionRepository,
            EnseignementRepository enseignementRepository,
            MatiereRepository matiereRepository,
            EntityMapper mapper,
            EntityReferenceService refs,
            TrimestreHelper trimestreHelper,
            PlanificationContextService planificationContext,
            BulletinExportService bulletinExportService) {
        this.noteRepository = noteRepository;
        this.evaluationRepository = evaluationRepository;
        this.eleveRepository = eleveRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.enseignementRepository = enseignementRepository;
        this.matiereRepository = matiereRepository;
        this.mapper = mapper;
        this.refs = refs;
        this.trimestreHelper = trimestreHelper;
        this.planificationContext = planificationContext;
        this.bulletinExportService = bulletinExportService;
    }

    @Transactional(readOnly = true)
    public List<BulletinEleveDto> listBulletins(Long classeId, Long niveauId) {
        List<Note> notes = noteRepository.findBulletinNotes(classeId, niveauId);
        Map<Long, Set<Long>> matieresByEleve = new LinkedHashMap<>();
        Map<Long, Eleve> eleves = new LinkedHashMap<>();
        for (Note note : notes) {
            Long eleveId = note.getEleve().getId();
            eleves.putIfAbsent(eleveId, note.getEleve());
            Long matiereId = note.getEvaluation().getEnseignement().getMatiere().getId();
            matieresByEleve.computeIfAbsent(eleveId, k -> new LinkedHashSet<>()).add(matiereId);
        }
        return eleves.values().stream()
                .map(e -> mapper.toBulletinDto(
                        e,
                        mapper.classeFromNotes(notes, e.getId()),
                        matieresByEleve.getOrDefault(e.getId(), Set.of()).size()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotesSummaryLigneDto> getSummary(Long classeId, Long niveauId) {
        if (classeId == null) {
            return List.of();
        }

        List<Note> notes = noteRepository.findBulletinNotes(classeId, niveauId);
        Map<Long, Set<Long>> matieresByEleve = new HashMap<>();
        for (Note note : notes) {
            Long eleveId = note.getEleve().getId();
            Long matiereId = note.getEvaluation().getEnseignement().getMatiere().getId();
            matieresByEleve.computeIfAbsent(eleveId, k -> new LinkedHashSet<>()).add(matiereId);
        }

        return eleveRepository.search("", niveauId, classeId).stream()
                .map(e -> {
                    String classe = inscriptionRepository.findActiveForEleve(e.getId())
                            .map(i -> i.getClasse().getLibelleComplet())
                            .orElse("—");
                    int count = matieresByEleve.getOrDefault(e.getId(), Set.of()).size();
                    return new NotesSummaryLigneDto(e.getId(), e.getNomComplet(), classe, count);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public GrilleNotesDto getGrille(Long classeId, Long niveauId, Long matiereId) {
        if (classeId == null) {
            return new GrilleNotesDto(List.of(), List.of());
        }

        List<Evaluation> evaluations = evaluationRepository.findByClasseAndMatiere(classeId, matiereId);
        List<GrilleColonneDto> colonnes = evaluations.stream()
                .map(ev -> {
                    var mat = ev.getEnseignement().getMatiere();
                    String libelle = ev.getType() + " (" + ev.getDateEvaluation() + " /" + ev.getNoteMax() + ")";
                    return new GrilleColonneDto(
                            ev.getId(),
                            mat.getNom(),
                            mat.getId(),
                            ev.getType(),
                            ev.getDateEvaluation(),
                            ev.getNoteMax(),
                            libelle);
                })
                .toList();

        List<Long> evalIds = evaluations.stream().map(Evaluation::getId).toList();
        Map<Long, Map<Long, BigDecimal>> notesByEleve = new HashMap<>();
        if (!evalIds.isEmpty()) {
            for (Note note : noteRepository.findByEvaluationIdIn(evalIds)) {
                notesByEleve
                        .computeIfAbsent(note.getEleve().getId(), k -> new HashMap<>())
                        .put(note.getEvaluation().getId(), note.getNoteObtenue());
            }
        }

        List<Eleve> eleves = eleveRepository.search("", niveauId, classeId);
        List<GrilleLigneDto> lignes = eleves.stream()
                .map(e -> {
                    String classe = inscriptionRepository.findActiveForEleve(e.getId())
                            .map(i -> i.getClasse().getLibelleComplet())
                            .orElse("—");
                    Map<Long, BigDecimal> notes = notesByEleve.getOrDefault(e.getId(), Map.of());
                    List<BigDecimal> row = evalIds.stream()
                            .map(id -> notes.get(id))
                            .toList();
                    return new GrilleLigneDto(e.getId(), e.getNomComplet(), classe, row);
                })
                .toList();

        return new GrilleNotesDto(colonnes, lignes);
    }

    @Transactional(readOnly = true)
    public BulletinDetailDto getBulletinDetail(Long eleveId) {
        Eleve eleve = refs.requireEleve(eleveId);
        String classe = inscriptionRepository.findActiveForEleve(eleveId)
                .map(i -> i.getClasse().getLibelleComplet())
                .orElse("—");
        List<BulletinLigneDto> lignes = noteRepository.findByEleveId(eleveId).stream()
                .map(n -> {
                    Evaluation ev = n.getEvaluation();
                    return new BulletinLigneDto(
                            ev.getEnseignement().getMatiere().getNom(),
                            ev.getType(),
                            ev.getDateEvaluation(),
                            ev.getNoteMax(),
                            n.getNoteObtenue());
                })
                .toList();
        return new BulletinDetailDto(eleveId, eleve.getNomComplet(), classe, lignes);
    }

    public NotesImportResultDto importNotes(List<NotesImportLigneDto> lignes) {
        int importees = 0;
        int ignorees = 0;
        int erreurs = 0;

        for (NotesImportLigneDto ligne : lignes) {
            try {
                if (importLine(ligne)) {
                    importees++;
                } else {
                    ignorees++;
                }
            } catch (Exception ex) {
                erreurs++;
            }
        }
        return new NotesImportResultDto(importees, ignorees, erreurs);
    }

    private boolean importLine(NotesImportLigneDto ligne) {
        Eleve eleve = eleveRepository.findByMatricule(ligne.matricule().trim()).orElse(null);
        if (eleve == null) {
            return false;
        }

        Inscription inscription = inscriptionRepository.findActiveForEleve(eleve.getId()).orElse(null);
        if (inscription == null) {
            return false;
        }

        Matiere matiere = matiereRepository.findByNomIgnoreCase(ligne.matiere().trim()).orElse(null);
        if (matiere == null) {
            return false;
        }

        Long classeId = inscription.getClasse().getId();
        Enseignement enseignement = enseignementRepository.search(classeId, matiere.getId()).stream()
                .findFirst()
                .orElse(null);
        if (enseignement == null) {
            return false;
        }

        Evaluation evaluation = evaluationRepository
                .findByEnseignementAndTypeAndDate(
                        enseignement.getId(),
                        ligne.typeEvaluation().trim(),
                        ligne.dateEvaluation())
                .orElseGet(() -> {
                    Evaluation ev = new Evaluation();
                    ev.setEnseignement(enseignement);
                    ev.setType(ligne.typeEvaluation().trim());
                    ev.setDateEvaluation(ligne.dateEvaluation());
                    ev.setNoteMax(ligne.noteMax());
                    AnneeScolaire annee = refs.activeAnnee();
                    ev.setTrimestre((short) trimestreHelper.trimestreCourant(annee));
                    return evaluationRepository.save(ev);
                });

        Note note = noteRepository
                .findByEvaluationIdAndEleveId(evaluation.getId(), eleve.getId())
                .orElseGet(() -> {
                    Note n = new Note();
                    n.setEvaluation(evaluation);
                    n.setEleve(eleve);
                    return n;
                });
        note.setNoteObtenue(ligne.noteObtenue());
        noteRepository.save(note);
        return true;
    }

    @Transactional(readOnly = true)
    public List<EvaluationDto> listEvaluations(Long classeId) {
        return evaluationRepository.findByClasseId(classeId).stream()
                .map(mapper::toEvaluationDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public EvaluationDto getEvaluation(Long id) {
        return mapper.toEvaluationDto(refs.requireEvaluation(id));
    }

    public EvaluationDto createEvaluation(EvaluationRequest request) {
        Evaluation e = new Evaluation();
        e.setEnseignement(refs.requireEnseignement(request.enseignementId()));
        e.setType(request.type());
        e.setDateEvaluation(request.dateEvaluation());
        e.setNoteMax(request.noteMax());
        e.setTrimestre(resolveTrimestre(request.trimestre()));
        return mapper.toEvaluationDto(evaluationRepository.save(e));
    }

    public EvaluationDto updateEvaluation(Long id, EvaluationRequest request) {
        Evaluation e = refs.requireEvaluation(id);
        e.setEnseignement(refs.requireEnseignement(request.enseignementId()));
        e.setType(request.type());
        e.setDateEvaluation(request.dateEvaluation());
        e.setNoteMax(request.noteMax());
        if (request.trimestre() != null) {
            e.setTrimestre(request.trimestre());
        }
        return mapper.toEvaluationDto(evaluationRepository.save(e));
    }

    public void deleteEvaluation(Long id) {
        Evaluation e = refs.requireEvaluation(id);
        noteRepository.findByEvaluationId(id).forEach(noteRepository::delete);
        evaluationRepository.delete(e);
    }

    @Transactional(readOnly = true)
    public List<NoteDto> listNotesByEvaluation(Long evaluationId) {
        return noteRepository.findByEvaluationId(evaluationId).stream()
                .map(mapper::toNoteDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoteDto getNote(Long id) {
        return mapper.toNoteDto(refs.requireNote(id));
    }

    public NoteDto createNote(NoteRequest request) {
        Note n = new Note();
        n.setEvaluation(refs.requireEvaluation(request.evaluationId()));
        n.setEleve(refs.requireEleve(request.eleveId()));
        n.setNoteObtenue(request.noteObtenue());
        return mapper.toNoteDto(noteRepository.save(n));
    }

    public NoteDto updateNote(Long id, NoteRequest request) {
        Note n = refs.requireNote(id);
        n.setEvaluation(refs.requireEvaluation(request.evaluationId()));
        n.setEleve(refs.requireEleve(request.eleveId()));
        n.setNoteObtenue(request.noteObtenue());
        return mapper.toNoteDto(noteRepository.save(n));
    }

    public void deleteNote(Long id) {
        noteRepository.delete(refs.requireNote(id));
    }

    @Transactional(readOnly = true)
    public TrimestreInfoDto getTrimestreCourant() {
        AnneeScolaire annee = refs.activeAnnee();
        TrimestreCourant courant = planificationContext.resolveTrimestreCourant(annee);
        return new TrimestreInfoDto(courant.numero(), courant.libelle());
    }

    public SaisieNotesDto getSaisie(Long classeId, Long matiereId) {
        if (classeId == null || matiereId == null) {
            throw new BadRequestException("La classe et la matière sont obligatoires.");
        }
        AnneeScolaire annee = refs.activeAnnee();
        TrimestreCourant trimestreCourant = planificationContext.resolveTrimestreCourant(annee);
        Enseignement enseignement = enseignementRepository.search(classeId, matiereId).stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Aucune affectation pour cette classe et matière. Créez-la dans Affectations."));
        Matiere matiere = refs.requireMatiere(matiereId);
        Long niveauId = enseignement.getClasse().getNiveau().getId();
        List<EvaluationPlan> plans = planificationContext.evaluationPlans(
                annee, niveauId, trimestreCourant.numero());

        List<Evaluation> evaluations = plans.stream()
                .map(plan -> ensureEvaluation(
                        enseignement,
                        plan.typeLabel(),
                        trimestreCourant.numero(),
                        annee,
                        trimestreCourant.trimestreId(),
                        plan.noteMax()))
                .toList();

        List<SaisieColonneDto> colonnes = evaluations.stream()
                .map(ev -> new SaisieColonneDto(ev.getId(), ev.getType(), ev.getNoteMax()))
                .toList();

        List<Eleve> eleves = eleveRepository.search("", null, classeId);
        List<SaisieLigneDto> lignes = eleves.stream()
                .map(eleve -> new SaisieLigneDto(
                        eleve.getId(),
                        eleve.getNomComplet(),
                        evaluations.stream()
                                .map(ev -> toSaisieCell(ev, eleve.getId()))
                                .toList()))
                .toList();

        return new SaisieNotesDto(
                trimestreCourant.numero(),
                trimestreCourant.libelle(),
                matiere.getNom(),
                colonnes,
                lignes);
    }

    public void saveSaisie(SaisieSaveRequest request) {
        for (SaisieNoteItemDto item : request.notes()) {
            if (item.notes() == null) {
                continue;
            }
            for (SaisieNoteValueDto value : item.notes()) {
                if (value.noteObtenue() != null) {
                    upsertNote(value.evaluationId(), item.eleveId(), value.noteObtenue());
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<BulletinEleveStatutDto> listBulletinStatuts(Long classeId, Long niveauId) {
        if (classeId == null) {
            return List.of();
        }
        return eleveRepository.search("", niveauId, classeId).stream()
                .map(eleve -> {
                    String classe = inscriptionRepository.findActiveForEleve(eleve.getId())
                            .map(i -> i.getClasse().getLibelleComplet())
                            .orElse("—");
                    return new BulletinEleveStatutDto(
                            eleve.getId(),
                            eleve.getNomComplet(),
                            classe,
                            isTrimestreComplet(eleve.getId(), classeId, 1),
                            isTrimestreComplet(eleve.getId(), classeId, 2),
                            isTrimestreComplet(eleve.getId(), classeId, 3));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public BulletinTrimestreDto getBulletinTrimestre(Long eleveId, int trimestre) {
        if (trimestre < 1 || trimestre > 3) {
            throw new BadRequestException("Le trimestre doit être entre 1 et 3.");
        }
        Eleve eleve = refs.requireEleve(eleveId);
        Inscription inscription = inscriptionRepository.findActiveForEleve(eleveId)
                .orElseThrow(() -> new BadRequestException("Aucune inscription active pour cet élève."));
        Long classeId = inscription.getClasse().getId();
        if (!isTrimestreComplet(eleveId, classeId, trimestre)) {
            throw new BadRequestException(
                    "Le bulletin n'est pas disponible : toutes les matières doivent avoir un devoir et une composition saisis.");
        }
        return buildBulletinTrimestre(eleve, inscription, trimestre);
    }

    @Transactional(readOnly = true)
    public byte[] exportBulletinPdf(Long eleveId, int trimestre) {
        return bulletinExportService.toPdf(getBulletinTrimestre(eleveId, trimestre));
    }

    @Transactional(readOnly = true)
    public byte[] exportBulletinExcel(Long eleveId, int trimestre) {
        return bulletinExportService.toExcel(getBulletinTrimestre(eleveId, trimestre));
    }

    private BulletinTrimestreDto buildBulletinTrimestre(Eleve eleve, Inscription inscription, int trimestre) {
        Long classeId = inscription.getClasse().getId();
        List<Enseignement> enseignements = enseignementRepository.findByClasseId(classeId);
        List<BulletinMatiereDto> matieres = new ArrayList<>();
        BigDecimal sommePonderee = BigDecimal.ZERO;
        BigDecimal sommeCoeffs = BigDecimal.ZERO;

        for (Enseignement en : enseignements) {
            Evaluation devoir = evaluationRepository
                    .findByEnseignementAndTypeAndTrimestre(en.getId(), NoteConstants.TYPE_DEVOIR, (short) trimestre)
                    .orElseThrow();
            Evaluation composition = evaluationRepository
                    .findByEnseignementAndTypeAndTrimestre(
                            en.getId(), NoteConstants.TYPE_COMPOSITION, (short) trimestre)
                    .orElseThrow();
            BigDecimal noteDevoir = noteRepository
                    .findByEvaluationIdAndEleveId(devoir.getId(), eleve.getId())
                    .map(Note::getNoteObtenue)
                    .orElseThrow();
            BigDecimal noteComposition = noteRepository
                    .findByEvaluationIdAndEleveId(composition.getId(), eleve.getId())
                    .map(Note::getNoteObtenue)
                    .orElseThrow();
            BigDecimal moyenne = noteDevoir.add(noteComposition)
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            BigDecimal coeff = en.getMatiere().getCoefficient();
            matieres.add(new BulletinMatiereDto(
                    en.getMatiere().getNom(), coeff, noteDevoir, noteComposition, moyenne));
            sommePonderee = sommePonderee.add(moyenne.multiply(coeff));
            sommeCoeffs = sommeCoeffs.add(coeff);
        }

        BigDecimal moyenneGenerale = sommeCoeffs.compareTo(BigDecimal.ZERO) > 0
                ? sommePonderee.divide(sommeCoeffs, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        AnneeScolaire annee = inscription.getAnneeScolaire();
        String ecole = annee.getEcole().getNom();

        return new BulletinTrimestreDto(
                eleve.getId(),
                eleve.getNomComplet(),
                inscription.getClasse().getLibelleComplet(),
                ecole,
                annee.getNom(),
                trimestre,
                trimestreHelper.libelle(trimestre),
                matieres,
                moyenneGenerale);
    }

    private boolean isTrimestreComplet(Long eleveId, Long classeId, int trimestre) {
        List<Enseignement> enseignements = enseignementRepository.findByClasseId(classeId);
        if (enseignements.isEmpty()) {
            return false;
        }
        for (Enseignement en : enseignements) {
            Optional<Evaluation> devoir = evaluationRepository.findByEnseignementAndTypeAndTrimestre(
                    en.getId(), NoteConstants.TYPE_DEVOIR, (short) trimestre);
            Optional<Evaluation> composition = evaluationRepository.findByEnseignementAndTypeAndTrimestre(
                    en.getId(), NoteConstants.TYPE_COMPOSITION, (short) trimestre);
            if (devoir.isEmpty() || composition.isEmpty()) {
                return false;
            }
            if (noteRepository.findByEvaluationIdAndEleveId(devoir.get().getId(), eleveId).isEmpty()) {
                return false;
            }
            if (noteRepository.findByEvaluationIdAndEleveId(composition.get().getId(), eleveId).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private Evaluation ensureEvaluation(
            Enseignement enseignement,
            String type,
            int trimestre,
            AnneeScolaire annee,
            Long trimestreId,
            BigDecimal noteMax) {
        return evaluationRepository
                .findByEnseignementAndTypeAndTrimestre(enseignement.getId(), type, (short) trimestre)
                .orElseGet(() -> {
                    Evaluation ev = new Evaluation();
                    ev.setEnseignement(enseignement);
                    ev.setType(type);
                    ev.setTrimestre((short) trimestre);
                    ev.setDateEvaluation(evaluationDate(annee, trimestre, trimestreId));
                    ev.setNoteMax(noteMax != null ? noteMax : BigDecimal.valueOf(20));
                    return evaluationRepository.save(ev);
                });
    }

    private java.time.LocalDate evaluationDate(AnneeScolaire annee, int trimestreNumero, Long trimestreId) {
        if (trimestreId != null) {
            return refs.requireTrimestre(trimestreId).getDateDebut();
        }
        return trimestreHelper.dateReference(annee, trimestreNumero);
    }

    private SaisieNoteCellDto toSaisieCell(Evaluation evaluation, Long eleveId) {
        Optional<Note> note = noteRepository.findByEvaluationIdAndEleveId(evaluation.getId(), eleveId);
        return new SaisieNoteCellDto(
                evaluation.getId(),
                note.map(Note::getId).orElse(null),
                note.map(Note::getNoteObtenue).orElse(null),
                evaluation.getNoteMax());
    }

    private void upsertNote(Long evaluationId, Long eleveId, BigDecimal noteObtenue) {
        Note note = noteRepository
                .findByEvaluationIdAndEleveId(evaluationId, eleveId)
                .orElseGet(() -> {
                    Note n = new Note();
                    n.setEvaluation(refs.requireEvaluation(evaluationId));
                    n.setEleve(refs.requireEleve(eleveId));
                    return n;
                });
        note.setNoteObtenue(noteObtenue);
        noteRepository.save(note);
    }

    private short resolveTrimestre(Short trimestre) {
        if (trimestre != null && trimestre >= 1 && trimestre <= 3) {
            return trimestre;
        }
        return (short) trimestreHelper.trimestreCourant(refs.activeAnnee());
    }
}
