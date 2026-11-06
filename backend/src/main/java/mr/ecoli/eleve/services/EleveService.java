package mr.ecoli.eleve.services;

import java.util.List;
import mr.ecoli.common.BillingBalanceService;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.eleve.dtos.EleveDto;
import mr.ecoli.eleve.dtos.EleveRequest;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.Eleve;
import mr.ecoli.dao.entities.Inscription;
import mr.ecoli.dao.repositories.EleveRepository;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.facturation.services.FactureService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EleveService {

    private final EleveRepository eleveRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;
    private final BillingBalanceService billing;
    private final FactureService factureService;

    public EleveService(
            EleveRepository eleveRepository,
            InscriptionRepository inscriptionRepository,
            EntityMapper mapper,
            EntityReferenceService refs,
            BillingBalanceService billing,
            FactureService factureService) {
        this.eleveRepository = eleveRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.mapper = mapper;
        this.refs = refs;
        this.billing = billing;
        this.factureService = factureService;
    }
    @Transactional(readOnly = true)
    public List<EleveDto> listEleves(String search, Long niveauId, Long classeId) {
        return eleveRepository.search(normalize(search), niveauId, classeId).stream()
                .map(e -> toDto(e))
                .toList();
    }
    @Transactional(readOnly = true)
    public EleveDto getById(Long id) {
        return toDto(refs.requireEleve(id));
    }
    public EleveDto create(EleveRequest request) {
        if (eleveRepository.existsByMatricule(request.matricule())) {
            throw new BadRequestException("Matricule déjà utilisé : " + request.matricule());
        }

        Eleve eleve = new Eleve();
        apply(eleve, request);
        eleve = eleveRepository.save(eleve);

        if (request.classeId() != null) {
            Inscription ins = new Inscription();
            ins.setEleve(eleve);
            ins.setClasse(refs.requireClasse(request.classeId()));
            ins.setAnneeScolaire(refs.activeAnnee());
            ins.setStatut("ACTIF");
            inscriptionRepository.save(ins);
            factureService.generateForEleve(eleve, refs.activeAnnee());
        }
        return toDto(eleve);
    }
    public EleveDto update(Long id, EleveRequest request) {
        Eleve eleve = refs.requireEleve(id);
        apply(eleve, request);
        eleve = eleveRepository.save(eleve);

        if (request.classeId() != null) {
            var annee = refs.activeAnnee();
            var existing = inscriptionRepository.findActiveForEleve(id);
            if (existing.isPresent()) {
                Inscription ins = existing.get();
                ins.setClasse(refs.requireClasse(request.classeId()));
                ins.setStatut("ACTIF");
                inscriptionRepository.save(ins);
            } else {
                Inscription ins = new Inscription();
                ins.setEleve(eleve);
                ins.setClasse(refs.requireClasse(request.classeId()));
                ins.setAnneeScolaire(annee);
                ins.setStatut("ACTIF");
                inscriptionRepository.save(ins);
            }
        }
        return toDto(eleve);
    }
    public void delete(Long id) {
        refs.requireEleve(id);
        inscriptionRepository.deleteByEleveId(id);
        eleveRepository.deleteById(id);
    }

    private void apply(Eleve eleve, EleveRequest request) {
        eleve.setMatricule(request.matricule());
        eleve.setNom(request.nom());
        eleve.setPrenom(request.prenom());
        eleve.setDateNaissance(request.dateNaissance());
        eleve.setSexe(request.sexe());
        eleve.setTuteur(request.tuteurId() != null
                ? refs.requireTuteur(request.tuteurId())
                : refs.unassignedTuteur());
        eleve.setEcole(request.ecoleId() != null ? refs.requireEcole(request.ecoleId()) : refs.defaultEcole());
    }

    private EleveDto toDto(Eleve eleve) {
        Inscription ins = inscriptionRepository.findActiveForEleve(eleve.getId()).orElse(null);
        return mapper.toEleveDto(
                eleve,
                ins,
                billing.getSoldeEleve(eleve.getId()),
                billing.getStatutEleve(eleve.getId()));
    }

    private String normalize(String search) {
        return search == null ? "" : search.trim();
    }
}
