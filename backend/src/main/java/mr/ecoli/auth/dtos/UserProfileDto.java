package mr.ecoli.auth.dtos;

public record UserProfileDto(
        Long id,
        String email,
        String nom,
        String prenom,
        String role,
        String avatarInitials,
        Long tuteurId
) {}
