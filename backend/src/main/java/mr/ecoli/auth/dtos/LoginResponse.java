package mr.ecoli.auth.dtos;

public record LoginResponse(
        String token,
        UserProfileDto user
) {}
