import 'package:ecoli_parent/core/api/api_client.dart';
import 'package:ecoli_parent/core/api/api_exception.dart';
import 'package:ecoli_parent/core/storage/token_storage.dart';
import 'package:ecoli_parent/models/user_profile.dart';

class AuthService {
  AuthService(this._client, this._tokenStorage);

  final ApiClient _client;
  final TokenStorage _tokenStorage;

  Future<LoginResponse> login(String email, String password) async {
    final response = await _client.post(
      '/auth/login',
      {'email': email.trim(), 'password': password},
      (json) => LoginResponse.fromJson(json as Map<String, dynamic>),
    );
    if (response.user.role != 'PARENT') {
      await _tokenStorage.clear();
      throw ApiException(
        'Ce compte n\'est pas un compte parent. Utilisez un compte PARENT.',
      );
    }
    await _tokenStorage.saveToken(response.token);
    return response;
  }

  Future<UserProfile> getMe() => _client.get(
        '/auth/me',
        (json) => UserProfile.fromJson(json as Map<String, dynamic>),
      );

  Future<void> logout() => _tokenStorage.clear();

  Future<bool> hasToken() async {
    final t = await _tokenStorage.readToken();
    return t != null && t.isNotEmpty;
  }
}
