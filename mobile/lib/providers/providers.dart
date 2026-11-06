import 'package:ecoli_parent/core/api/api_client.dart';
import 'package:ecoli_parent/core/storage/token_storage.dart';
import 'package:ecoli_parent/models/user_profile.dart';
import 'package:ecoli_parent/providers/parent_polling.dart';
import 'package:ecoli_parent/services/auth_service.dart';
import 'package:ecoli_parent/services/parent_app_service.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final tokenStorageProvider = Provider<TokenStorage>((ref) => TokenStorage());

final apiClientProvider = Provider<ApiClient>((ref) {
  return ApiClient(ref.watch(tokenStorageProvider));
});

final authServiceProvider = Provider<AuthService>((ref) {
  return AuthService(ref.watch(apiClientProvider), ref.watch(tokenStorageProvider));
});

final parentAppServiceProvider = Provider<ParentAppService>((ref) {
  return ParentAppService(ref.watch(apiClientProvider));
});

final authStateProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  return AuthNotifier(ref.watch(authServiceProvider));
});

class AuthState {
  const AuthState({this.user, this.isLoading = false, this.initialized = false});

  final UserProfile? user;
  final bool isLoading;
  final bool initialized;

  bool get isLoggedIn => user != null;

  AuthState copyWith({UserProfile? user, bool? isLoading, bool? initialized}) {
    return AuthState(
      user: user ?? this.user,
      isLoading: isLoading ?? this.isLoading,
      initialized: initialized ?? this.initialized,
    );
  }
}

class AuthNotifier extends StateNotifier<AuthState> {
  AuthNotifier(this._auth) : super(const AuthState());

  final AuthService _auth;

  Future<void> initSession() async {
    state = state.copyWith(isLoading: true);
    try {
      if (await _auth.hasToken()) {
        final user = await _auth.getMe();
        if (user.role == 'PARENT') {
          state = AuthState(user: user, initialized: true);
          return;
        }
        await _auth.logout();
      }
      state = const AuthState(initialized: true);
    } catch (_) {
      await _auth.logout();
      state = const AuthState(initialized: true);
    } finally {
      state = state.copyWith(isLoading: false);
    }
  }

  Future<void> login(String email, String password) async {
    state = state.copyWith(isLoading: true);
    try {
      final res = await _auth.login(email, password);
      state = AuthState(user: res.user, initialized: true);
    } catch (_) {
      rethrow;
    } finally {
      state = state.copyWith(isLoading: false);
    }
  }

  Future<void> logout() async {
    await _auth.logout();
    state = const AuthState(initialized: true);
  }
}

final unreadCountProvider = FutureProvider<int>((ref) async {
  ref.watch(parentDataPollTickProvider);
  final auth = ref.watch(authStateProvider);
  if (!auth.isLoggedIn) return 0;
  return ref.read(parentAppServiceProvider).getUnreadCount();
});
