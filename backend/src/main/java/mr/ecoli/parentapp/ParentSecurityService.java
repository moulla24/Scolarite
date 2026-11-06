package mr.ecoli.parentapp;

import mr.ecoli.common.exception.ForbiddenException;
import mr.ecoli.common.exception.ResourceNotFoundException;
import mr.ecoli.dao.entities.Tuteur;
import mr.ecoli.dao.repositories.EleveRepository;
import mr.ecoli.dao.repositories.TuteurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ParentSecurityService {

    private final TuteurRepository tuteurRepository;
    private final EleveRepository eleveRepository;

    public ParentSecurityService(TuteurRepository tuteurRepository, EleveRepository eleveRepository) {
        this.tuteurRepository = tuteurRepository;
        this.eleveRepository = eleveRepository;
    }

    public Long requireTuteurId() {
        return requireTuteur().getId();
    }

    public Tuteur requireTuteur() {
        Long userId = currentUserId();
        return tuteurRepository.findByUtilisateurId(userId)
                .orElseThrow(() -> new ForbiddenException("Compte parent non associé à un tuteur."));
    }

    public void requireEleveBelongsToParent(Long eleveId) {
        Long tuteurId = requireTuteurId();
        if (!eleveRepository.existsByIdAndTuteurId(eleveId, tuteurId)) {
            throw new ForbiddenException("Accès refusé à cet élève.");
        }
    }

    public void requireParentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().stream().noneMatch(a -> "ROLE_PARENT".equals(a.getAuthority()))) {
            throw new ForbiddenException("Réservé aux comptes parents.");
        }
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new ResourceNotFoundException("Utilisateur non authentifié.");
        }
        return userId;
    }
}
