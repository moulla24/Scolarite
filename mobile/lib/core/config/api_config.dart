/// URL de base de l'API backend.
/// - Émulateur Android : 10.0.2.2 → localhost du PC
/// - Simulateur iOS / Windows : localhost
/// - Appareil physique : IP LAN du PC (ex. 192.168.1.10)
class ApiConfig {
  ApiConfig._();

  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8081/api',
  );
}
