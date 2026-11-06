class ParentDashboard {
  const ParentDashboard({
    required this.nombreEnfants,
    required this.notificationsNonLues,
    required this.soldeGlobal,
    required this.statutCompte,
  });

  final int nombreEnfants;
  final int notificationsNonLues;
  final double soldeGlobal;
  final String statutCompte;

  factory ParentDashboard.fromJson(Map<String, dynamic> json) => ParentDashboard(
        nombreEnfants: json['nombreEnfants'] as int,
        notificationsNonLues: (json['notificationsNonLues'] as num).toInt(),
        soldeGlobal: (json['soldeGlobal'] as num).toDouble(),
        statutCompte: json['statutCompte'] as String,
      );
}

class EnfantResume {
  const EnfantResume({
    required this.id,
    required this.matricule,
    required this.nomComplet,
    this.sexe,
    this.dateNaissance,
    required this.ecoleNom,
    required this.classe,
    required this.niveau,
    required this.anneeScolaire,
    required this.statutFacturation,
    required this.soldeRestant,
  });

  final int id;
  final String matricule;
  final String nomComplet;
  final String? sexe;
  final String? dateNaissance;
  final String ecoleNom;
  final String classe;
  final String niveau;
  final String anneeScolaire;
  final String statutFacturation;
  final double soldeRestant;

  factory EnfantResume.fromJson(Map<String, dynamic> json) => EnfantResume(
        id: json['id'] as int,
        matricule: json['matricule'] as String,
        nomComplet: json['nomComplet'] as String,
        sexe: json['sexe'] as String?,
        dateNaissance: json['dateNaissance'] as String?,
        ecoleNom: json['ecoleNom'] as String,
        classe: json['classe'] as String,
        niveau: json['niveau'] as String,
        anneeScolaire: json['anneeScolaire'] as String,
        statutFacturation: json['statutFacturation'] as String,
        soldeRestant: (json['soldeRestant'] as num).toDouble(),
      );
}

class Scolarite {
  const Scolarite({
    required this.eleveId,
    required this.nomComplet,
    required this.matricule,
    required this.ecoleNom,
    required this.ecoleCode,
    this.ecoleTelephone,
    this.ecoleEmail,
    required this.classe,
    required this.niveau,
    this.salle,
    required this.anneeScolaire,
    this.anneeDebut,
    this.anneeFin,
    required this.statutInscription,
    this.dateInscription,
  });

  final int eleveId;
  final String nomComplet;
  final String matricule;
  final String ecoleNom;
  final String ecoleCode;
  final String? ecoleTelephone;
  final String? ecoleEmail;
  final String classe;
  final String niveau;
  final String? salle;
  final String anneeScolaire;
  final String? anneeDebut;
  final String? anneeFin;
  final String statutInscription;
  final String? dateInscription;

  factory Scolarite.fromJson(Map<String, dynamic> json) => Scolarite(
        eleveId: json['eleveId'] as int,
        nomComplet: json['nomComplet'] as String,
        matricule: json['matricule'] as String,
        ecoleNom: json['ecoleNom'] as String,
        ecoleCode: json['ecoleCode'] as String,
        ecoleTelephone: json['ecoleTelephone'] as String?,
        ecoleEmail: json['ecoleEmail'] as String?,
        classe: json['classe'] as String,
        niveau: json['niveau'] as String,
        salle: json['salle'] as String?,
        anneeScolaire: json['anneeScolaire'] as String,
        anneeDebut: json['anneeDebut'] as String?,
        anneeFin: json['anneeFin'] as String?,
        statutInscription: json['statutInscription'] as String,
        dateInscription: json['dateInscription'] as String?,
      );
}

class BulletinLigne {
  const BulletinLigne({
    required this.matiere,
    required this.type,
    this.dateEvaluation,
    required this.noteMax,
    required this.noteObtenue,
  });

  final String matiere;
  final String type;
  final String? dateEvaluation;
  final double noteMax;
  final double noteObtenue;

  factory BulletinLigne.fromJson(Map<String, dynamic> json) => BulletinLigne(
        matiere: json['matiere'] as String,
        type: json['type'] as String,
        dateEvaluation: json['dateEvaluation'] as String?,
        noteMax: (json['noteMax'] as num).toDouble(),
        noteObtenue: (json['noteObtenue'] as num).toDouble(),
      );
}

class Bulletin {
  const Bulletin({
    required this.eleveId,
    required this.nomComplet,
    required this.classe,
    required this.lignes,
  });

  final int eleveId;
  final String nomComplet;
  final String classe;
  final List<BulletinLigne> lignes;

  factory Bulletin.fromJson(Map<String, dynamic> json) => Bulletin(
        eleveId: json['eleveId'] as int,
        nomComplet: json['nomComplet'] as String,
        classe: json['classe'] as String,
        lignes: (json['lignes'] as List<dynamic>)
            .map((e) => BulletinLigne.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

class Absence {
  const Absence({
    required this.id,
    required this.date,
    required this.matiere,
    required this.statut,
    this.motif,
    required this.horaire,
  });

  final int id;
  final String date;
  final String matiere;
  final String statut;
  final String? motif;
  final String horaire;

  bool get isAbsent => statut == 'ABSENT';

  factory Absence.fromJson(Map<String, dynamic> json) => Absence(
        id: json['id'] as int,
        date: json['date'] as String,
        matiere: json['matiere'] as String,
        statut: json['statut'] as String,
        motif: json['motif'] as String?,
        horaire: json['horaire'] as String,
      );
}

class SeanceEmploi {
  const SeanceEmploi({
    required this.id,
    required this.jour,
    required this.jourIndex,
    required this.horaire,
    required this.classe,
    required this.matiere,
    required this.enseignant,
    this.salle,
  });

  final int id;
  final String jour;
  final int jourIndex;
  final String horaire;
  final String classe;
  final String matiere;
  final String enseignant;
  final String? salle;

  factory SeanceEmploi.fromJson(Map<String, dynamic> json) => SeanceEmploi(
        id: json['id'] as int,
        jour: json['jour'] as String,
        jourIndex: json['jourIndex'] as int,
        horaire: json['horaire'] as String,
        classe: json['classe'] as String,
        matiere: json['matiere'] as String,
        enseignant: json['enseignant'] as String,
        salle: json['salle'] as String?,
      );
}

class Facture {
  const Facture({
    required this.id,
    required this.numero,
    required this.codePaiement,
    required this.eleveId,
    required this.eleveNom,
    required this.dateEmission,
    required this.dateEcheance,
    required this.montantTotal,
    required this.montantPaye,
    required this.resteAPayer,
    required this.statut,
  });

  final int id;
  final String numero;
  final String codePaiement;
  final int eleveId;
  final String eleveNom;
  final String dateEmission;
  final String dateEcheance;
  final double montantTotal;
  final double montantPaye;
  final double resteAPayer;
  final String statut;

  factory Facture.fromJson(Map<String, dynamic> json) => Facture(
        id: json['id'] as int,
        numero: json['numero'] as String,
        codePaiement: (json['codePaiement'] as String?) ??
            (json['numero'] as String).replaceFirst('FAC-', 'PAY-'),
        eleveId: json['eleveId'] as int,
        eleveNom: json['eleveNom'] as String,
        dateEmission: json['dateEmission'] as String,
        dateEcheance: json['dateEcheance'] as String,
        montantTotal: (json['montantTotal'] as num).toDouble(),
        montantPaye: (json['montantPaye'] as num).toDouble(),
        resteAPayer: (json['resteAPayer'] as num).toDouble(),
        statut: json['statut'] as String,
      );
}

class DemandePaiement {
  const DemandePaiement({
    required this.id,
    required this.factureId,
    required this.factureNumero,
    required this.eleveNom,
    required this.montant,
    required this.datePaiement,
    required this.modePaiement,
    this.reference,
    this.preuveReference,
    required this.statut,
    this.motifRefus,
    this.paiementId,
    this.numeroRecu,
    required this.dateCreation,
    this.dateTraitement,
  });

  final int id;
  final int factureId;
  final String factureNumero;
  final String eleveNom;
  final double montant;
  final String datePaiement;
  final String modePaiement;
  final String? reference;
  final String? preuveReference;
  final String statut;
  final String? motifRefus;
  final int? paiementId;
  final String? numeroRecu;
  final String dateCreation;
  final String? dateTraitement;

  factory DemandePaiement.fromJson(Map<String, dynamic> json) => DemandePaiement(
        id: json['id'] as int,
        factureId: json['factureId'] as int,
        factureNumero: json['factureNumero'] as String,
        eleveNom: json['eleveNom'] as String,
        montant: (json['montant'] as num).toDouble(),
        datePaiement: json['datePaiement'] as String,
        modePaiement: json['modePaiement'] as String,
        reference: json['reference'] as String?,
        preuveReference: json['preuveReference'] as String?,
        statut: json['statut'] as String,
        motifRefus: json['motifRefus'] as String?,
        paiementId: json['paiementId'] as int?,
        numeroRecu: json['numeroRecu'] as String?,
        dateCreation: json['dateCreation'] as String,
        dateTraitement: json['dateTraitement'] as String?,
      );
}

class DemandePaiementRequest {
  const DemandePaiementRequest({
    required this.factureId,
    required this.montant,
    this.datePaiement,
    this.modePaiement,
    this.reference,
    this.preuveReference,
  });

  final int factureId;
  final double montant;
  final String? datePaiement;
  final String? modePaiement;
  final String? reference;
  final String? preuveReference;

  Map<String, dynamic> toJson() => {
        'factureId': factureId,
        'montant': montant,
        if (datePaiement != null) 'datePaiement': datePaiement,
        if (modePaiement != null) 'modePaiement': modePaiement,
        if (reference != null && reference!.isNotEmpty) 'reference': reference,
        if (preuveReference != null && preuveReference!.isNotEmpty)
          'preuveReference': preuveReference,
      };
}

class ParentNotification {
  const ParentNotification({
    required this.id,
    required this.type,
    required this.titre,
    required this.message,
    required this.lu,
    required this.dateCreation,
    this.eleveId,
    this.eleveNom,
  });

  final int id;
  final String type;
  final String titre;
  final String message;
  final bool lu;
  final String dateCreation;
  final int? eleveId;
  final String? eleveNom;

  factory ParentNotification.fromJson(Map<String, dynamic> json) =>
      ParentNotification(
        id: json['id'] as int,
        type: json['type'] as String,
        titre: json['titre'] as String,
        message: json['message'] as String,
        lu: json['lu'] as bool,
        dateCreation: json['dateCreation'] as String,
        eleveId: json['eleveId'] as int?,
        eleveNom: json['eleveNom'] as String?,
      );
}

List<T> parseList<T>(dynamic data, T Function(Map<String, dynamic>) fromJson) {
  return (data as List<dynamic>)
      .map((e) => fromJson(e as Map<String, dynamic>))
      .toList();
}
