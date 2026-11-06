package mr.ecoli.auth.services;

import mr.ecoli.auth.dtos.LoginRequest;
import mr.ecoli.auth.dtos.LoginResponse;
import mr.ecoli.auth.dtos.UserProfileDto;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.common.exception.ResourceNotFoundException;
import mr.ecoli.dao.entities.Utilisateur;
import mr.ecoli.dao.repositories.TuteurRepository;
import mr.ecoli.dao.repositories.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final TuteurRepository tuteurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UtilisateurRepository utilisateurRepository,
            TuteurRepository tuteurRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.tuteurRepository = tuteurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Utilisateur user = utilisateurRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("Identifiants invalides."));
        if (!user.isActif()) {
            throw new BadRequestException("Compte désactivé.");
        }
        if (!passwordEncoder.matches(request.password(), user.getMotDePasse())) {
            throw new BadRequestException("Identifiants invalides.");
        }
        Long tuteurId = resolveTuteurId(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole(), tuteurId);
        return new LoginResponse(token, toProfile(user, tuteurId));
    }

    public UserProfileDto getProfile(Long userId) {
        Utilisateur user = requireUser(userId);
        return toProfile(user, resolveTuteurId(user));
    }

    public Utilisateur requireUser(Long userId) {
        return utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + userId));
    }

    private UserProfileDto toProfile(Utilisateur user, Long tuteurId) {
        String initials = initials(user.getPrenom(), user.getNom());
        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getRole(),
                initials,
                tuteurId);
    }

    private Long resolveTuteurId(Utilisateur user) {
        if (!"PARENT".equals(user.getRole())) {
            return null;
        }
        return tuteurRepository.findByUtilisateurId(user.getId())
                .map(t -> t.getId())
                .orElse(null);
    }

    private String initials(String prenom, String nom) {
        String p = prenom != null && !prenom.isBlank() ? prenom.substring(0, 1) : "";
        String n = nom != null && !nom.isBlank() ? nom.substring(0, 1) : "";
        return (p + n).toUpperCase();
    }
}
