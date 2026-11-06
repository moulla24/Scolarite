package mr.ecoli.common;

import mr.ecoli.common.exception.ResourceNotFoundException;
import mr.ecoli.dao.entities.AnneeScolaire;
import mr.ecoli.dao.entities.Classe;
import mr.ecoli.dao.entities.Ecole;
import mr.ecoli.dao.entities.Eleve;
import mr.ecoli.dao.entities.Comptable;
import mr.ecoli.dao.entities.Enseignant;
import mr.ecoli.dao.entities.Enseignement;
import mr.ecoli.dao.entities.Evaluation;
import mr.ecoli.dao.entities.MaquettePedagogique;
import mr.ecoli.dao.entities.Matiere;
import mr.ecoli.dao.entities.Niveau;
import mr.ecoli.dao.entities.Note;
import mr.ecoli.dao.entities.Semaine;
import mr.ecoli.dao.entities.SeancePresence;
import mr.ecoli.dao.entities.Trimestre;
import mr.ecoli.dao.entities.TypeControle;
import mr.ecoli.dao.entities.Tuteur;
import mr.ecoli.dao.repositories.AnneeScolaireRepository;
import mr.ecoli.dao.repositories.ClasseRepository;
import mr.ecoli.dao.repositories.EcoleRepository;
import mr.ecoli.dao.repositories.EleveRepository;
import mr.ecoli.dao.repositories.ComptableRepository;
import mr.ecoli.dao.repositories.EnseignantRepository;
import mr.ecoli.dao.repositories.EnseignementRepository;
import mr.ecoli.dao.repositories.EvaluationRepository;
import mr.ecoli.dao.repositories.MaquettePedagogiqueRepository;
import mr.ecoli.dao.repositories.MatiereRepository;
import mr.ecoli.dao.repositories.NiveauRepository;
import mr.ecoli.dao.repositories.NoteRepository;
import mr.ecoli.dao.repositories.SemaineRepository;
import mr.ecoli.dao.repositories.SeancePresenceRepository;
import mr.ecoli.dao.repositories.TrimestreRepository;
import mr.ecoli.dao.repositories.TypeControleRepository;
import mr.ecoli.dao.repositories.TuteurRepository;
import org.springframework.stereotype.Service;

import static mr.ecoli.common.SystemTuteurConstants.UNASSIGNED_NOM;
import static mr.ecoli.common.SystemTuteurConstants.UNASSIGNED_TELEPHONE;

@Service
public class EntityReferenceService {

    private final TuteurRepository tuteurRepository;
    private final EleveRepository eleveRepository;
    private final ClasseRepository classeRepository;
    private final NiveauRepository niveauRepository;
    private final MatiereRepository matiereRepository;
    private final EnseignantRepository enseignantRepository;
    private final ComptableRepository comptableRepository;
    private final EnseignementRepository enseignementRepository;
    private final MaquettePedagogiqueRepository maquetteRepository;
    private final SemaineRepository semaineRepository;
    private final SeancePresenceRepository seanceRepository;
    private final EvaluationRepository evaluationRepository;
    private final NoteRepository noteRepository;
    private final EcoleRepository ecoleRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final TrimestreRepository trimestreRepository;
    private final TypeControleRepository typeControleRepository;

    public EntityReferenceService(
            TuteurRepository tuteurRepository,
            EleveRepository eleveRepository,
            ClasseRepository classeRepository,
            NiveauRepository niveauRepository,
            MatiereRepository matiereRepository,
            EnseignantRepository enseignantRepository,
            ComptableRepository comptableRepository,
            EnseignementRepository enseignementRepository,
            MaquettePedagogiqueRepository maquetteRepository,
            SemaineRepository semaineRepository,
            SeancePresenceRepository seanceRepository,
            EvaluationRepository evaluationRepository,
            NoteRepository noteRepository,
            EcoleRepository ecoleRepository,
            AnneeScolaireRepository anneeScolaireRepository,
            TrimestreRepository trimestreRepository,
            TypeControleRepository typeControleRepository) {
        this.tuteurRepository = tuteurRepository;
        this.eleveRepository = eleveRepository;
        this.classeRepository = classeRepository;
        this.niveauRepository = niveauRepository;
        this.matiereRepository = matiereRepository;
        this.enseignantRepository = enseignantRepository;
        this.comptableRepository = comptableRepository;
        this.enseignementRepository = enseignementRepository;
        this.maquetteRepository = maquetteRepository;
        this.semaineRepository = semaineRepository;
        this.seanceRepository = seanceRepository;
        this.evaluationRepository = evaluationRepository;
        this.noteRepository = noteRepository;
        this.ecoleRepository = ecoleRepository;
        this.anneeScolaireRepository = anneeScolaireRepository;
        this.trimestreRepository = trimestreRepository;
        this.typeControleRepository = typeControleRepository;
    }

    public Tuteur requireTuteur(Long id) {
        return tuteurRepository.findById(id).orElseThrow(() -> notFound("Parent", id));
    }

    public Tuteur unassignedTuteur() {
        return tuteurRepository.findByNomAndTelephone(UNASSIGNED_NOM, UNASSIGNED_TELEPHONE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parent système « À affecter » introuvable. Exécutez les migrations Flyway."));
    }

    public boolean isUnassignedTuteur(Tuteur tuteur) {
        return tuteur != null
                && UNASSIGNED_NOM.equals(tuteur.getNom())
                && UNASSIGNED_TELEPHONE.equals(tuteur.getTelephone());
    }

    public Eleve requireEleve(Long id) {
        return eleveRepository.findById(id).orElseThrow(() -> notFound("Élève", id));
    }

    public Classe requireClasse(Long id) {
        return classeRepository.findById(id).orElseThrow(() -> notFound("Classe", id));
    }

    public Niveau requireNiveau(Long id) {
        return niveauRepository.findById(id).orElseThrow(() -> notFound("Niveau", id));
    }

    public Matiere requireMatiere(Long id) {
        return matiereRepository.findById(id).orElseThrow(() -> notFound("Matière", id));
    }

    public Matiere requireMatiereByNom(String nom) {
        return matiereRepository
                .findByNomIgnoreCase(nom.trim())
                .orElseThrow(() -> new mr.ecoli.common.exception.BadRequestException(
                        "Matière introuvable : " + nom));
    }

    public Enseignant requireEnseignant(Long id) {
        return enseignantRepository.findById(id).orElseThrow(() -> notFound("Enseignant", id));
    }

    public Comptable requireComptable(Long id) {
        return comptableRepository.findById(id).orElseThrow(() -> notFound("Comptable", id));
    }

    public Enseignement requireEnseignement(Long id) {
        return enseignementRepository.findById(id).orElseThrow(() -> notFound("Enseignement", id));
    }

    public MaquettePedagogique requireMaquette(Long id) {
        return maquetteRepository.findById(id).orElseThrow(() -> notFound("Maquette", id));
    }

    public Semaine requireSemaine(Long id) {
        return semaineRepository.findById(id).orElseThrow(() -> notFound("Semaine", id));
    }

    public SeancePresence requireSeance(Long id) {
        return seanceRepository.findById(id).orElseThrow(() -> notFound("Seance", id));
    }

    public Evaluation requireEvaluation(Long id) {
        return evaluationRepository.findById(id).orElseThrow(() -> notFound("Évaluation", id));
    }

    public Note requireNote(Long id) {
        return noteRepository.findById(id).orElseThrow(() -> notFound("Note", id));
    }

    public Ecole requireEcole(Long id) {
        return ecoleRepository.findById(id).orElseThrow(() -> notFound("École", id));
    }

    public Ecole defaultEcole() {
        return ecoleRepository.findById(2L).orElseGet(() ->
                ecoleRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Aucune école configurée")));
    }

    public AnneeScolaire requireAnneeScolaire(Long id) {
        return anneeScolaireRepository.findById(id)
                .orElseThrow(() -> notFound("Année scolaire", id));
    }

    public Trimestre requireTrimestre(Long id) {
        return trimestreRepository.findById(id).orElseThrow(() -> notFound("Trimestre", id));
    }

    public TypeControle requireTypeControle(Long id) {
        return typeControleRepository.findById(id).orElseThrow(() -> notFound("Type de contrôle", id));
    }

    public AnneeScolaire activeAnnee() {
        return anneeScolaireRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Aucune année scolaire active"));
    }

    private ResourceNotFoundException notFound(String label, Long id) {
        return new ResourceNotFoundException(label + " introuvable : " + id);
    }
}
