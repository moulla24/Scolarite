package mr.ecoli.comptabilite.services;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mr.ecoli.comptabilite.dtos.PaiePersonnelLigneDto;
import mr.ecoli.comptabilite.dtos.PaiementPersonnelDto;
import mr.ecoli.comptabilite.dtos.PaiementPersonnelRequest;
import mr.ecoli.comptabilite.dtos.PersonnelDto;
import mr.ecoli.comptabilite.dtos.PersonnelRequest;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.common.exception.ResourceNotFoundException;
import mr.ecoli.dao.entities.Enseignant;
import mr.ecoli.dao.entities.PaiementPersonnel;
import mr.ecoli.dao.entities.Personnel;
import mr.ecoli.dao.repositories.EnseignantRepository;
import mr.ecoli.dao.repositories.PaiementPersonnelRepository;
import mr.ecoli.dao.repositories.PersonnelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PersonnelService {

    private static final Map<String, String> CATEGORIES = Map.of(
            "ENSEIGNANT", "Enseignant",
            "AGENT_ENTRETIEN", "Agent d'entretien",
            "GARDIEN", "Gardien",
            "SECRETAIRE", "Secrétaire",
            "AUTRE", "Autre personnel");

    private final PersonnelRepository personnelRepository;
    private final PaiementPersonnelRepository paiementPersonnelRepository;
    private final EnseignantRepository enseignantRepository;
    private final ComptabiliteJournalService journalService;

    public PersonnelService(
            PersonnelRepository personnelRepository,
            PaiementPersonnelRepository paiementPersonnelRepository,
            EnseignantRepository enseignantRepository,
            ComptabiliteJournalService journalService) {
        this.personnelRepository = personnelRepository;
        this.paiementPersonnelRepository = paiementPersonnelRepository;
        this.enseignantRepository = enseignantRepository;
        this.journalService = journalService;
    }

    @Transactional(readOnly = true)
    public List<PersonnelDto> list(String categorie, String statut, String search) {
        return personnelRepository.search(normalize(categorie), normalize(statut), normalize(search)).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PersonnelDto getById(Long id) {
        return toDto(requirePersonnel(id));
    }

    public PersonnelDto create(PersonnelRequest request) {
        validateCategorie(request.categorie());
        Personnel p = new Personnel();
        applyRequest(p, request);
        return toDto(personnelRepository.save(p));
    }

    public PersonnelDto update(Long id, PersonnelRequest request) {
        validateCategorie(request.categorie());
        Personnel p = requirePersonnel(id);
        applyRequest(p, request);
        return toDto(personnelRepository.save(p));
    }

    public void delete(Long id) {
        Personnel p = requirePersonnel(id);
        if (p.getEnseignant() != null) {
            throw new BadRequestException("Impossible de supprimer un enseignant lié — désactivez-le plutôt.");
        }
        personnelRepository.delete(p);
    }

    @Transactional(readOnly = true)
    public List<PaiePersonnelLigneDto> listPaieMois(YearMonth mois) {
        LocalDate moisDebut = mois.atDay(1);
        List<Personnel> actifs = personnelRepository.search("", "ACTIF", "");
        Map<Long, PaiementPersonnel> payes = new HashMap<>();
        for (PaiementPersonnel pp : paiementPersonnelRepository.findByMois(moisDebut)) {
            payes.put(pp.getPersonnel().getId(), pp);
        }
        return actifs.stream().map(p -> {
            PaiementPersonnel pp = payes.get(p.getId());
            return new PaiePersonnelLigneDto(
                    p.getId(),
                    p.getNomComplet(),
                    p.getMatricule(),
                    p.getCategorie(),
                    libelleCategorie(p.getCategorie()),
                    p.getSalaireMensuel(),
                    pp != null,
                    pp != null ? pp.getId() : null,
                    pp != null ? pp.getMontant() : null,
                    pp != null ? pp.getDatePaiement() : null,
                    pp != null ? pp.getModePaiement() : null);
        }).toList();
    }

    public PaiementPersonnelDto payer(PaiementPersonnelRequest request) {
        Personnel p = requirePersonnel(request.personnelId());
        if (!"ACTIF".equals(p.getStatut())) {
            throw new BadRequestException("Ce personnel n'est pas actif.");
        }
        LocalDate mois = YearMonth.from(request.mois()).atDay(1);
        if (paiementPersonnelRepository.findByPersonnelIdAndMois(p.getId(), mois).isPresent()) {
            throw new BadRequestException("Salaire déjà payé pour ce mois.");
        }

        PaiementPersonnel pp = new PaiementPersonnel();
        pp.setPersonnel(p);
        pp.setMois(mois);
        pp.setMontant(request.montant());
        pp.setModePaiement(request.modePaiement());
        pp.setDatePaiement(request.datePaiement() != null ? request.datePaiement() : LocalDate.now());
        pp.setReference(request.reference());
        pp = paiementPersonnelRepository.save(pp);

        journalService.enregistrerPaiePersonnel(pp);
        return toPaiementDto(pp);
    }

    public List<PaiementPersonnelDto> payerTous(YearMonth mois, String modePaiement) {
        return listPaieMois(mois).stream()
                .filter(l -> !l.paye())
                .map(l -> payer(new PaiementPersonnelRequest(
                        l.personnelId(),
                        mois.atDay(1),
                        l.salaireMensuel(),
                        modePaiement,
                        LocalDate.now(),
                        "PAIE-" + mois)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, String> categories() {
        return CATEGORIES;
    }

    private void applyRequest(Personnel p, PersonnelRequest request) {
        p.setNom(request.nom().trim());
        p.setPrenom(request.prenom().trim());
        p.setEmail(blankToNull(request.email()));
        p.setTelephone(blankToNull(request.telephone()));
        p.setMatricule(blankToNull(request.matricule()));
        p.setCategorie(request.categorie().trim().toUpperCase());
        p.setSalaireMensuel(request.salaireMensuel());
        p.setDateEmbauche(request.dateEmbauche());
        p.setStatut(request.statut().trim().toUpperCase());

        if ("ENSEIGNANT".equals(p.getCategorie())) {
            if (request.enseignantId() == null) {
                throw new BadRequestException("Un enseignant doit être sélectionné pour la catégorie ENSEIGNANT.");
            }
            Enseignant e = enseignantRepository.findById(request.enseignantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enseignant introuvable"));
            personnelRepository.findByEnseignantId(e.getId()).ifPresent(existing -> {
                if (!existing.getId().equals(p.getId())) {
                    throw new BadRequestException("Cet enseignant est déjà enregistré comme personnel.");
                }
            });
            p.setEnseignant(e);
        } else {
            p.setEnseignant(null);
        }
    }

    private Personnel requirePersonnel(Long id) {
        return personnelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personnel introuvable : " + id));
    }

    private PersonnelDto toDto(Personnel p) {
        return new PersonnelDto(
                p.getId(),
                p.getNom(),
                p.getPrenom(),
                p.getNomComplet(),
                p.getEmail(),
                p.getTelephone(),
                p.getMatricule(),
                p.getCategorie(),
                libelleCategorie(p.getCategorie()),
                p.getEnseignant() != null ? p.getEnseignant().getId() : null,
                p.getSalaireMensuel(),
                p.getDateEmbauche(),
                p.getStatut());
    }

    private PaiementPersonnelDto toPaiementDto(PaiementPersonnel pp) {
        Personnel p = pp.getPersonnel();
        return new PaiementPersonnelDto(
                pp.getId(),
                p.getId(),
                p.getNomComplet(),
                libelleCategorie(p.getCategorie()),
                pp.getMois(),
                pp.getMontant(),
                pp.getModePaiement(),
                pp.getDatePaiement(),
                pp.getReference());
    }

    private void validateCategorie(String categorie) {
        if (!CATEGORIES.containsKey(categorie.toUpperCase())) {
            throw new BadRequestException("Catégorie invalide : " + categorie);
        }
    }

    private String libelleCategorie(String categorie) {
        return CATEGORIES.getOrDefault(categorie, categorie);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
