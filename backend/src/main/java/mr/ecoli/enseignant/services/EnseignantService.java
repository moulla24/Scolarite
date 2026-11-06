package mr.ecoli.enseignant.services;

import java.util.List;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.enseignant.dtos.EnseignantDto;
import mr.ecoli.enseignant.dtos.EnseignantRequest;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.Enseignant;
import mr.ecoli.dao.entities.Utilisateur;
import mr.ecoli.dao.repositories.EnseignantRepository;
import mr.ecoli.dao.repositories.EnseignementRepository;
import mr.ecoli.dao.repositories.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EnseignantService {

    private final EnseignantRepository enseignantRepository;
    private final EnseignementRepository enseignementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;
    private final PasswordEncoder passwordEncoder;

    public EnseignantService(
            EnseignantRepository enseignantRepository,
            EnseignementRepository enseignementRepository,
            UtilisateurRepository utilisateurRepository,
            EntityMapper mapper,
            EntityReferenceService refs,
            PasswordEncoder passwordEncoder) {
        this.enseignantRepository = enseignantRepository;
        this.enseignementRepository = enseignementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.mapper = mapper;
        this.refs = refs;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional(readOnly = true)
    public List<EnseignantDto> listEnseignants(String search) {
        return enseignantRepository.search(normalize(search)).stream()
                .map(e -> mapper.toEnseignantDto(e, enseignementRepository.findByEnseignantId(e.getId())))
                .toList();
    }
    @Transactional(readOnly = true)
    public EnseignantDto getById(Long id) {
        Enseignant e = refs.requireEnseignant(id);
        return mapper.toEnseignantDto(e, enseignementRepository.findByEnseignantId(id));
    }
    public EnseignantDto create(EnseignantRequest request) {
        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email déjà utilisé : " + request.email());
        }
        Utilisateur u = new Utilisateur();
        applyUser(u, request);
        u.setMotDePasse(passwordEncoder.encode("admin123"));
        u.setRole("ENSEIGNANT");
        u = utilisateurRepository.save(u);

        Enseignant e = new Enseignant();
        e.setUtilisateur(u);
        e.setSpecialite(request.specialite());
        e.setDateEmbauche(request.dateEmbauche());
        e = enseignantRepository.save(e);
        return mapper.toEnseignantDto(e, List.of());
    }
    public EnseignantDto update(Long id, EnseignantRequest request) {
        Enseignant e = refs.requireEnseignant(id);
        applyUser(e.getUtilisateur(), request);
        e.setSpecialite(request.specialite());
        e.setDateEmbauche(request.dateEmbauche());
        e.getUtilisateur().setActif(request.actif());
        e = enseignantRepository.save(e);
        return mapper.toEnseignantDto(e, enseignementRepository.findByEnseignantId(id));
    }
    public void delete(Long id) {
        Enseignant e = refs.requireEnseignant(id);
        if (!enseignementRepository.findByEnseignantId(id).isEmpty()) {
            throw new BadRequestException("Impossible de supprimer : l'enseignant a des affectations.");
        }
        enseignantRepository.delete(e);
        utilisateurRepository.delete(e.getUtilisateur());
    }

    private void applyUser(Utilisateur u, EnseignantRequest request) {
        u.setNom(request.nom());
        u.setPrenom(request.prenom());
        u.setEmail(request.email());
        u.setTelephone(request.telephone());
        u.setActif(request.actif());
    }

    private String normalize(String search) {
        return search == null ? "" : search.trim();
    }
}
