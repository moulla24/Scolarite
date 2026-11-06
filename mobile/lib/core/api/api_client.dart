import 'package:dio/dio.dart';
import 'package:ecoli_parent/core/api/api_exception.dart';
import 'package:ecoli_parent/core/config/api_config.dart';
import 'package:ecoli_parent/core/storage/token_storage.dart';

class ApiClient {
  ApiClient(this._tokenStorage) {
    _dio = Dio(
      BaseOptions(
        baseUrl: ApiConfig.baseUrl,
        connectTimeout: const Duration(seconds: 15),
        receiveTimeout: const Duration(seconds: 15),
        headers: {'Content-Type': 'application/json', 'Accept': 'application/json'},
      ),
    );
    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          if (!options.path.contains('/auth/login')) {
            final token = await _tokenStorage.readToken();
            if (token != null && token.isNotEmpty) {
              options.headers['Authorization'] = 'Bearer $token';
            }
          }
          handler.next(options);
        },
        onError: (error, handler) {
          handler.next(error);
        },
      ),
    );
  }

  final TokenStorage _tokenStorage;
  late final Dio _dio;

  Dio get dio => _dio;

  Future<T> get<T>(String path, T Function(dynamic json) parser) async {
    return _request(() => _dio.get(path), parser);
  }

  Future<T> post<T>(
    String path,
    dynamic body,
    T Function(dynamic json) parser,
  ) async {
    return _request(() => _dio.post(path, data: body), parser);
  }

  Future<void> put(String path) async {
    try {
      await _dio.put(path);
    } on DioException catch (e) {
      throw _mapError(e);
    }
  }

  Future<void> postVoid(String path, {Map<String, dynamic>? body}) async {
    try {
      await _dio.post(path, data: body);
    } on DioException catch (e) {
      throw _mapError(e);
    }
  }

  Future<T> _request<T>(
    Future<Response<dynamic>> Function() call,
    T Function(dynamic json) parser,
  ) async {
    try {
      final response = await call();
      final data = response.data;
      if (data == null || (data is String && data.isEmpty)) {
        if (T == Null || null is T) return null as T;
        throw ApiException('Réponse vide du serveur');
      }
      return parser(data);
    } on DioException catch (e) {
      throw _mapError(e);
    }
  }

  ApiException _mapError(DioException e) {
    final status = e.response?.statusCode;
    final data = e.response?.data;
    String message = 'Erreur réseau';
    if (data is Map && data['message'] != null) {
      message = data['message'].toString();
    } else if (e.message != null) {
      message = e.message!;
    }
    if (status == null || status == 0) {
      message = 'Impossible de joindre le serveur (CORS ou backend arrêté). Redémarrez le backend sur le port 8081.';
    }
    if (status == 401) message = 'Session expirée. Reconnectez-vous.';
    if (status == 400) {
      if (message == 'Erreur réseau' || message.isEmpty) {
        message = 'Identifiants invalides.';
      }
    }
    return ApiException(message, statusCode: status);
  }
}
