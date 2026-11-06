package mr.ecoli.common.mapper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mr.ecoli.dao.entities.AnneeScolaire;
import mr.ecoli.anneescolaire.dtos.AnneeScolaireDto;
import mr.ecoli.note.dtos.BulletinEleveDto;
import mr.ecoli.ecole.dtos.EcoleDto;
import mr.ecoli.classe.dtos.ClasseDto;
import mr.ecoli.eleve.dtos.EleveDto;
import mr.ecoli.comptable.dtos.ComptableDto;
import mr.ecoli.enseignant.dtos.EnseignantDto;
import mr.ecoli.evaluation.dtos.EvaluationDto;
import mr.ecoli.maquette.dtos.MaquetteDto;
import mr.ecoli.matiere.dtos.MatiereDto;
import mr.ecoli.note.dtos.NoteDto;
import mr.ecoli.niveau.dtos.NiveauDto;
import mr.ecoli.seance.dtos.SeanceEmploiDto;
import mr.ecoli.tuteur.dtos.EleveAffecteDto;
import mr.ecoli.tuteur.dtos.TuteurDto;
import mr.ecoli.dao.entities.Ecole;
import mr.ecoli.dao.entities.Evaluation;
import mr.ecoli.dao.entities.Matiere;
import mr.ecoli.dao.entities.Classe;
import mr.ecoli.dao.entities.Eleve;
import mr.ecoli.dao.entities.Comptable;
import mr.ecoli.dao.entities.Enseignant;
import mr.ecoli.dao.entities.Enseignement;
import mr.ecoli.dao.entities.Inscription;
import mr.ecoli.dao.entities.MaquettePedagogique;
import mr.ecoli.dao.entities.Niveau;
import mr.ecoli.dao.entities.Note;
import mr.ecoli.dao.entities.SeancePresence;
import mr.ecoli.dao.entities.Trimestre;
import mr.ecoli.dao.entities.TrimestreTypeControle;
import mr.ecoli.dao.entities.TypeControle;
import mr.ecoli.dao.entities.Tuteur;
import mr.ecoli.planification.dtos.QuotaControleDto;
import mr.ecoli.planification.dtos.TrimestreDto;
import mr.ecoli.planification.dtos.TypeControleDto;
import mr.ecoli.dao.entities.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    private static final Map<Short, String> JOURS = Map.of(
            (short) 1, "Lundi",
            (short) 2, "Mardi",
            (short) 3, "Mercredi",
            (short) 4, "Jeudi",
            (short) 5, "Vendredi"
    );

    public TuteurDto toTuteurDto(
            Tuteur tuteur,
            List<Eleve> eleves,
            List<EleveAffecteDto> elevesAffectes,
            java.math.BigDecimal solde,
            String statutCompte) {
        List<String> noms = eleves.stream().map(Eleve::getNomComplet).toList();
        List<Long> ids = eleves.stream().map(Eleve::getId).toList();
        return new TuteurDto(
                tuteur.getId(),
                tuteur.getNom(),
                tuteur.getPrenom(),
                formatNom(tuteur.getPrenom(), tuteur.getNom()),
                tuteur.getTelephone(),
                tuteur.getEmail(),
                tuteur.getAdresse(),
                eleves.size(),
                noms,
                ids,
                elevesAffectes,
                solde,
                statutCompte
        );
    }

    public EleveDto toEleveDto(Eleve eleve, Inscription inscription, java.math.BigDecimal solde, String statut) {
        String classe = inscription != null ? inscription.getClasse().getLibelleComplet() : "-";
        String niveau = inscription != null ? inscription.getClasse().getNiveau().getNom() : "-";
        return new EleveDto(
                eleve.getId(),
                eleve.getNom(),
                eleve.getPrenom(),
                eleve.getNomComplet(),
                initials(eleve.getPrenom(), eleve.getNom()),
                eleve.getMatricule(),
                classe,
                niveau,
                eleve.getDateNaissance(),
                eleve.getSexe(),
                solde,
                statut
        );
    }

    public ClasseDto toClasseDto(Classe classe, long effectif, String enseignantReferent) {
        int capacite = classe.getCapacite();
        int taux = capacite > 0 ? (int) Math.round((effectif * 100.0) / capacite) : 0;
        return new ClasseDto(
                classe.getId(),
                classe.getLibelleComplet(),
                classe.getNiveau().getNom(),
                enseignantReferent != null ? enseignantReferent : "—",
                effectif,
                capacite,
                classe.getSalle(),
                taux
        );
    }

    public EnseignantDto toEnseignantDto(Enseignant enseignant, List<Enseignement> enseignements) {
        Utilisateur u = enseignant.getUtilisateur();
        var matieres = enseignements.stream().map(e -> e.getMatiere().getNom()).collect(Collectors.toCollection(LinkedHashSet::new));
        var classes = enseignements.stream().map(e -> e.getClasse().getLibelleComplet()).collect(Collectors.toCollection(LinkedHashSet::new));
        return new EnseignantDto(
                enseignant.getId(),
                u.getPrenom() + " " + u.getNom(),
                initials(u.getPrenom(), u.getNom()),
                u.getEmail(),
                u.getTelephone(),
                enseignant.getSpecialite(),
                List.copyOf(matieres),
                List.copyOf(classes),
                u.isActif() ? "ACTIF" : "EN_CONGE"
        );
    }

    public ComptableDto toComptableDto(Comptable comptable) {
        Utilisateur u = comptable.getUtilisateur();
        return new ComptableDto(
                comptable.getId(),
                u.getPrenom() + " " + u.getNom(),
                initials(u.getPrenom(), u.getNom()),
                u.getEmail(),
                u.getTelephone(),
                comptable.getMatricule(),
                comptable.getDateEmbauche(),
                u.isActif() ? "ACTIF" : "INACTIF"
        );
    }

    public MaquetteDto toMaquetteDto(MaquettePedagogique m) {
        return new MaquetteDto(
                m.getId(),
                m.getTitre(),
                m.getMatiere().getNom(),
                m.getNiveau().getNom(),
                m.getDescription(),
                m.getFormat(),
                m.getNbPages(),
                m.getDateMaj(),
                m.getFichierUrl()
        );
    }

    public BulletinEleveDto toBulletinDto(Eleve eleve, String classe, int nbMatieres) {
        return new BulletinEleveDto(eleve.getId(), eleve.getNomComplet(), classe, nbMatieres);
    }

    public SeanceEmploiDto toSeanceDto(SeancePresence s) {
        Utilisateur u = s.getEnseignant().getUtilisateur();
        return new SeanceEmploiDto(
                s.getId(),
                s.getSemaine().getId(),
                JOURS.getOrDefault(s.getJourSemaine(), "—"),
                s.getJourSemaine(),
                s.getHeureDebut() + " - " + s.getHeureFin(),
                s.getClasse().getId(),
                s.getClasse().getLibelleComplet(),
                s.getMatiere().getId(),
                s.getMatiere().getNom(),
                s.getEnseignant().getId(),
                u.getPrenom() + " " + u.getNom(),
                s.getSalle()
        );
    }

    public EcoleDto toEcoleDto(Ecole e) {
        return new EcoleDto(
                e.getId(),
                e.getNom(),
                e.getCode(),
                e.getAdresse(),
                e.getTelephone(),
                e.getEmail(),
                e.getLogoUrl());
    }

    public NiveauDto toNiveauDto(Niveau n) {
        return new NiveauDto(
                n.getId(),
                n.getEcole().getId(),
                n.getEcole().getNom(),
                n.getNom());
    }

    public AnneeScolaireDto toAnneeScolaireDto(AnneeScolaire a) {
        return new AnneeScolaireDto(
                a.getId(),
                a.getEcole().getId(),
                a.getEcole().getNom(),
                a.getNom(),
                a.getDateDebut(),
                a.getDateFin(),
                a.isActive());
    }

    public TrimestreDto toTrimestreDto(Trimestre t) {
        return new TrimestreDto(
                t.getId(),
                t.getAnneeScolaire().getId(),
                t.getAnneeScolaire().getNom(),
                t.getNumero(),
                t.getNom(),
                t.getDateDebut(),
                t.getDateFin(),
                t.isActif());
    }

    public TypeControleDto toTypeControleDto(TypeControle t) {
        var n = t.getNiveau();
        return new TypeControleDto(
                t.getId(),
                n.getId(),
                n.getNom(),
                n.getEcole().getId(),
                n.getEcole().getNom(),
                t.getCode(),
                t.getLibelle(),
                t.getNoteMax(),
                t.getOrdre(),
                t.isActif());
    }

    public QuotaControleDto toQuotaControleDto(TypeControle type, TrimestreTypeControle quota) {
        return new QuotaControleDto(
                quota != null ? quota.getId() : null,
                type.getId(),
                type.getCode(),
                type.getLibelle(),
                quota != null ? quota.getNombrePrevu().intValue() : 0);
    }

    public EvaluationDto toEvaluationDto(Evaluation e) {
        var en = e.getEnseignement();
        return new EvaluationDto(
                e.getId(),
                e.getType(),
                e.getDateEvaluation(),
                e.getNoteMax(),
                e.getTrimestre(),
                en.getClasse().getLibelleComplet(),
                en.getMatiere().getNom()
        );
    }

    public NoteDto toNoteDto(Note n) {
        return new NoteDto(
                n.getId(),
                n.getEvaluation().getId(),
                n.getEleve().getId(),
                n.getEleve().getNomComplet(),
                n.getNoteObtenue()
        );
    }

    public MatiereDto toMatiereDto(Matiere m) {
        return new MatiereDto(m.getId(), m.getNom(), m.getCode(), m.getCoefficient());
    }

    public String classeFromNotes(List<Note> notes, Long eleveId) {
        return notes.stream()
                .filter(n -> n.getEleve().getId().equals(eleveId))
                .findFirst()
                .map(n -> n.getEvaluation().getEnseignement().getClasse().getLibelleComplet())
                .orElse("-");
    }

    private String formatNom(String prenom, String nom) {
        if (prenom == null || prenom.isBlank()) {
            return nom;
        }
        return prenom + " " + nom;
    }

    private String initials(String prenom, String nom) {
        String p = prenom != null && !prenom.isBlank() ? prenom.substring(0, 1) : "";
        String n = nom != null && !nom.isBlank() ? nom.substring(0, 1) : "";
        return (p + n).toUpperCase();
    }
}
