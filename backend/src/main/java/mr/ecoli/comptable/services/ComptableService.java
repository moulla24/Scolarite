package mr.ecoli.comptable.services;

import java.util.List;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.comptable.dtos.ComptableDto;
import mr.ecoli.comptable.dtos.ComptableRequest;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.Comptable;
import mr.ecoli.dao.entities.Utilisateur;
import mr.ecoli.dao.repositories.ComptableRepository;
import mr.ecoli.dao.repositories.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ComptableService {

    private final ComptableRepository comptableRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;
    private final PasswordEncoder passwordEncoder;

    public ComptableService(
            ComptableRepository comptableRepository,
            UtilisateurRepository utilisateurRepository,
            EntityMapper mapper,
            EntityReferenceService refs,
            PasswordEncoder passwordEncoder) {
        this.comptableRepository = comptableRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.mapper = mapper;
        this.refs = refs;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<ComptableDto> list(String search) {
        return comptableRepository.search(normalize(search)).stream()
                .map(mapper::toComptableDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ComptableDto getById(Long id) {
        return mapper.toComptableDto(refs.requireComptable(id));
    }

    public ComptableDto create(ComptableRequest request) {
        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email déjà utilisé : " + request.email());
        }
        Utilisateur u = new Utilisateur();
        applyUser(u, request);
        u.setMotDePasse(passwordEncoder.encode("admin123"));
        u.setRole("COMPTABLE");
        u = utilisateurRepository.save(u);

        Comptable c = new Comptable();
        c.setUtilisateur(u);
        c.setMatricule(request.matricule());
        c.setDateEmbauche(request.dateEmbauche());
        c = comptableRepository.save(c);
        return mapper.toComptableDto(c);
    }

    public ComptableDto update(Long id, ComptableRequest request) {
        Comptable c = refs.requireComptable(id);
        Utilisateur u = c.getUtilisateur();
        if (!u.getEmail().equalsIgnoreCase(request.email())
                && utilisateurRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email déjà utilisé : " + request.email());
        }
        applyUser(u, request);
        c.setMatricule(request.matricule());
        c.setDateEmbauche(request.dateEmbauche());
        c = comptableRepository.save(c);
        return mapper.toComptableDto(c);
    }

    public void delete(Long id) {
        Comptable c = refs.requireComptable(id);
        comptableRepository.delete(c);
        utilisateurRepository.delete(c.getUtilisateur());
    }

    private void applyUser(Utilisateur u, ComptableRequest request) {
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
