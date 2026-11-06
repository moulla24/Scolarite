class UserProfile {
  const UserProfile({
    required this.id,
    required this.email,
    required this.nom,
    required this.prenom,
    required this.role,
    required this.avatarInitials,
    this.tuteurId,
  });

  final int id;
  final String email;
  final String nom;
  final String prenom;
  final String role;
  final String avatarInitials;
  final int? tuteurId;

  String get nomComplet => '$prenom $nom';

  factory UserProfile.fromJson(Map<String, dynamic> json) => UserProfile(
        id: json['id'] as int,
        email: json['email'] as String,
        nom: json['nom'] as String,
        prenom: json['prenom'] as String,
        role: json['role'] as String,
        avatarInitials: json['avatarInitials'] as String? ?? '',
        tuteurId: json['tuteurId'] as int?,
      );
}

class LoginResponse {
  const LoginResponse({required this.token, required this.user});

  final String token;
  final UserProfile user;

  factory LoginResponse.fromJson(Map<String, dynamic> json) => LoginResponse(
        token: json['token'] as String,
        user: UserProfile.fromJson(json['user'] as Map<String, dynamic>),
      );
}
