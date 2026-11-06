/** Réponses JSON du backend Spring (records Java). */

export interface DashboardStatsDto {
  elevesActifs: number;
  elevesInscrits: number;
  enseignantsActifs: number;
  classesTotal: number;
  seancesSemaine: number;
}

export interface DashboardActivityDto {
  reference: string;
  parentName: string;
  studentsCount: number;
  level: string;
  amount: number;
  date: string;
  status: string;
}

export interface NiveauDto {
  id: number;
  ecoleId: number;
  ecoleNom: string;
  nom: string;
}

export interface ClasseDto {
  id: number;
  nom: string;
  niveau: string;
  enseignantReferent: string;
  effectif: number;
  capacite: number;
  salle: string;
  tauxOccupation: number;
}

export interface MatiereDto {
  id: number;
  nom: string;
  code: string;
  coefficient: number;
}

export interface EleveAffecteDto {
  id: number;
  nomComplet: string;
  niveau: string;
  classe: string;
}

export interface TuteurDto {
  id: number;
  nom: string;
  prenom: string | null;
  nomComplet: string;
  telephone: string;
  email: string | null;
  adresse: string | null;
  nombreEleves: number;
  nomsEleves: string[];
  eleveIds: number[];
  elevesAffectes: EleveAffecteDto[];
  solde: number;
  statutCompte: string;
}

export interface EleveDto {
  id: number;
  nom: string;
  prenom: string;
  nomComplet: string;
  initials: string;
  matricule: string;
  classe: string;
  niveau: string;
  dateNaissance: string | null;
  sexe: string | null;
  solde: number;
  statut: string;
}

export interface BulletinEleveDto {
  eleveId: number;
  nomComplet: string;
  classe: string;
  nombreMatieres: number;
}

export interface EnseignantDto {
  id: number;
  nomComplet: string;
  initials: string;
  email: string;
  telephone: string;
  specialite: string;
  matieres: string[];
  classes: string[];
  statut: string;
}

export interface MaquetteDto {
  id: number;
  titre: string;
  matiere: string;
  niveau: string;
  description: string;
  format: string;
  nbPages: number;
  dateMaj: string;
  fichierUrl: string | null;
}

export interface MaquetteChapitreDto {
  id: number;
  ordre: number;
  titre: string;
  description: string | null;
}

export interface MaquetteMatiereCardDto {
  id: number;
  matiereId: number;
  matiere: string;
  volumeHoraire: number;
  nombreChapitres: number;
}

export interface MaquetteGrilleDto {
  maquetteClasseId: number;
  classeId: number;
  classe: string;
  niveau: string;
  matieres: MaquetteMatiereCardDto[];
}

export interface MaquetteMatiereDetailDto {
  id: number;
  matiereId: number;
  matiere: string;
  volumeHoraire: number;
  classe: string;
  niveau: string;
  chapitres: MaquetteChapitreDto[];
}

export interface MaquetteChapitreRequest {
  ordre?: number;
  titre: string;
  description?: string | null;
}

export interface MaquetteMatiereSaveRequest {
  volumeHoraire: number;
  chapitres: MaquetteChapitreRequest[];
}

export interface MaquetteAddMatiereRequest {
  nom: string;
  volumeHoraire: number;
  chapitres: MaquetteChapitreRequest[];
}

export interface SemaineDto {
  id: number;
  trimestreId: number;
  numero: number;
  dateDebut: string;
  dateFin: string;
  label: string;
}

export interface SeanceEmploiDto {
  id: number;
  semaineId: number;
  jour: string;
  jourIndex: number;
  horaire: string;
  classeId: number;
  classe: string;
  matiereId: number;
  matiere: string;
  enseignantId: number;
  enseignant: string;
  salle: string;
}

/** Corps de requête POST/PUT — aligné sur les records Java backend. */

export interface TuteurRequest {
  nom: string;
  prenom: string | null;
  email: string | null;
  telephone: string;
  adresse: string | null;
  eleveIds?: number[];
}

export interface EleveRequest {
  matricule: string;
  nom: string;
  prenom: string;
  dateNaissance: string | null;
  sexe: string | null;
  tuteurId?: number | null;
  ecoleId: number | null;
  classeId: number | null;
}

export interface EnseignantRequest {
  nom: string;
  prenom: string;
  email: string;
  telephone: string | null;
  specialite: string | null;
  dateEmbauche: string | null;
  actif: boolean;
}

export interface ClasseRequest {
  niveauId: number;
  nom: string;
  salle: string | null;
  capacite: number;
}

export interface EvaluationDto {
  id: number;
  type: string;
  dateEvaluation: string;
  noteMax: number;
  trimestre: number;
  classe: string;
  matiere: string;
}

export interface EvaluationRequest {
  enseignementId: number;
  type: string;
  dateEvaluation: string;
  noteMax: number;
  trimestre?: number;
}

export interface TrimestreInfoDto {
  courant: number;
  libelle: string;
}

export interface SaisieNoteCellDto {
  evaluationId: number;
  noteId: number | null;
  noteObtenue: number | null;
  noteMax: number;
}

export interface SaisieColonneDto {
  evaluationId: number;
  libelle: string;
  noteMax: number;
}

export interface SaisieLigneDto {
  eleveId: number;
  nomComplet: string;
  notes: SaisieNoteCellDto[];
}

export interface SaisieNotesDto {
  trimestreCourant: number;
  trimestreLibelle: string;
  matiere: string;
  colonnes: SaisieColonneDto[];
  lignes: SaisieLigneDto[];
}

export interface SaisieNoteValueDto {
  evaluationId: number;
  noteObtenue?: number | null;
}

export interface SaisieNoteItemDto {
  eleveId: number;
  notes: SaisieNoteValueDto[];
}

export interface SaisieSaveRequest {
  classeId: number;
  matiereId: number;
  notes: SaisieNoteItemDto[];
}

export interface BulletinEleveStatutDto {
  eleveId: number;
  nomComplet: string;
  classe: string;
  trimestre1Disponible: boolean;
  trimestre2Disponible: boolean;
  trimestre3Disponible: boolean;
}

export interface BulletinMatiereDto {
  matiere: string;
  coefficient: number;
  noteDevoir: number;
  noteComposition: number;
  moyenne: number;
}

export interface BulletinTrimestreDto {
  eleveId: number;
  nomComplet: string;
  classe: string;
  ecole: string;
  anneeScolaire: string;
  trimestre: number;
  trimestreLibelle: string;
  matieres: BulletinMatiereDto[];
  moyenneGenerale: number;
}

export interface EnseignementDto {
  id: number;
  classeId: number;
  classe: string;
  matiereId: number;
  matiere: string;
  enseignantId: number;
  enseignant: string;
  libelle: string;
}

export interface EnseignementRequest {
  classeId: number;
  matiereId: number;
  enseignantId: number;
}

export interface GrilleColonneDto {
  evaluationId: number;
  matiere: string;
  matiereId: number;
  type: string;
  dateEvaluation: string;
  noteMax: number;
  libelle: string;
}

export interface GrilleLigneDto {
  eleveId: number;
  nomComplet: string;
  classe: string;
  notes: (number | null)[];
}

export interface GrilleNotesDto {
  colonnes: GrilleColonneDto[];
  lignes: GrilleLigneDto[];
}

export interface BulletinLigneDto {
  matiere: string;
  type: string;
  dateEvaluation: string;
  noteMax: number;
  noteObtenue: number;
}

export interface BulletinDetailDto {
  eleveId: number;
  nomComplet: string;
  classe: string;
  lignes: BulletinLigneDto[];
}

export interface NotesImportLigneDto {
  matricule: string;
  matiere: string;
  typeEvaluation: string;
  dateEvaluation: string;
  noteMax: number;
  noteObtenue: number;
}

export interface NotesImportResultDto {
  importees: number;
  ignorees: number;
  erreurs: number;
}

export interface NotesSummaryLigneDto {
  eleveId: number;
  nomComplet: string;
  classe: string;
  nombreMatieres: number;
}

export interface NoteDto {
  id: number;
  evaluationId: number;
  eleveId: number;
  eleveNom: string;
  noteObtenue: number;
}

export interface NoteRequest {
  evaluationId: number;
  eleveId: number;
  noteObtenue: number;
}

export interface SeanceRequest {
  semaineId: number;
  classeId: number;
  matiereId: number;
  enseignantId: number;
  jourSemaine: number;
  heureDebut: string;
  heureFin: string;
  salle: string;
  type: string;
}

export interface EcoleDto {
  id: number;
  nom: string;
  code: string;
  adresse: string;
  telephone: string;
  email: string;
  logoUrl: string;
}

export interface EcoleRequest {
  nom: string;
  code: string;
  adresse?: string;
  telephone?: string;
  email?: string;
  logoUrl?: string;
}

export interface NiveauRequest {
  ecoleId: number;
  nom: string;
}

export interface AnneeScolaireDto {
  id: number;
  ecoleId: number;
  ecoleNom: string;
  nom: string;
  dateDebut: string;
  dateFin: string;
  active: boolean;
}

export interface AnneeScolaireRequest {
  ecoleId: number;
  nom: string;
  dateDebut: string;
  dateFin: string;
  active: boolean;
}

export interface TrimestreDto {
  id: number;
  anneeScolaireId: number;
  anneeScolaireNom: string;
  numero: number;
  nom: string;
  dateDebut: string;
  dateFin: string;
  actif: boolean;
}

export interface TrimestreRequest {
  anneeScolaireId: number;
  numero: number;
  nom: string;
  dateDebut: string;
  dateFin: string;
  actif: boolean;
}

export interface TypeControleDto {
  id: number;
  niveauId: number;
  niveauNom: string;
  ecoleId: number;
  ecoleNom: string;
  code: string;
  libelle: string;
  noteMax: number;
  ordre: number;
  actif: boolean;
}

export interface TypeControleRequest {
  niveauId: number;
  code: string;
  libelle: string;
  noteMax: number;
  ordre: number;
  actif: boolean;
}

export interface QuotaControleDto {
  id: number | null;
  typeControleId: number;
  typeControleCode: string;
  typeControleLibelle: string;
  nombrePrevu: number;
}

export interface QuotasControlesRequest {
  trimestreId: number;
  niveauId: number;
  quotas: readonly {
    typeControleId: number;
    nombrePrevu: number;
  }[];
}

export type PresenceStatut = 'PRESENT' | 'ABSENT';

export interface PresenceEleveLigneDto {
  eleveId: number;
  nomComplet: string;
  statut: PresenceStatut;
  motif: string | null;
}

export interface PlagePresenceDto {
  plage: string;
  libelle: string;
  eleves: PresenceEleveLigneDto[];
}

export interface FeuillePresenceDto {
  date: string;
  classeId: number;
  classeNom: string;
  jourSemaine: number;
  jourLibelle: string;
  jourScolaire: boolean;
  plages: PlagePresenceDto[];
}

export interface PresenceLigneSaveRequest {
  plage: string;
  eleveId: number;
  statut: PresenceStatut;
  motif?: string | null;
}

export interface FeuillePresenceSaveRequest {
  date: string;
  classeId: number;
  lignes: PresenceLigneSaveRequest[];
}

export interface ComptableDto {
  id: number;
  nomComplet: string;
  initials: string;
  email: string;
  telephone: string;
  matricule: string;
  dateEmbauche: string | null;
  statut: string;
}

export interface ComptableRequest {
  nom: string;
  prenom: string;
  email: string;
  telephone: string | null;
  matricule: string | null;
  dateEmbauche: string | null;
  actif: boolean;
}

export interface ComptabiliteStatsDto {
  encaissementsMois: number;
  creancesTotales: number;
  facturesEmises: number;
  facturesImpayees: number;
  paiementsMois: number;
  notificationsNonLues: number;
}

export interface FactureListDto {
  id: number;
  numero: string;
  codePaiement: string;
  eleveId: number;
  eleveNom: string;
  parentNom: string;
  parentTelephone: string;
  niveau: string;
  dateEmission: string;
  dateEcheance: string;
  montantTotal: number;
  montantPaye: number;
  resteAPayer: number;
  statut: string;
}

export interface LigneFactureDto {
  id: number;
  libelle: string;
  montant: number;
}

export interface PaiementHistoriqueDto {
  id: number;
  factureId: number;
  factureNumero: string;
  parentNom: string;
  eleveNom: string;
  niveau: string;
  montant: number;
  datePaiement: string;
  modePaiement: string;
  reference: string | null;
  numeroRecu: string | null;
  agentNom: string | null;
}

export interface FactureDetailDto extends FactureListDto {
  parentEmail: string;
  anneeScolaire: string;
  lignes: LigneFactureDto[];
  paiements: PaiementHistoriqueDto[];
}

export interface EcritureComptableDto {
  id: number;
  dateEcriture: string;
  numeroPiece: string;
  libelle: string;
  compteDebit: string;
  compteCredit: string;
  compteDebitLibelle: string;
  compteCreditLibelle: string;
  montant: number;
  referenceType: string | null;
  referenceId: number | null;
  modifiable: boolean;
}

export interface EcritureComptableRequest {
  dateEcriture: string;
  libelle: string;
  compteDebit: string;
  compteCredit: string;
  montant: number;
  numeroPiece?: string | null;
}

export interface BilanLigneDto {
  compte: string;
  libelle: string;
  montant: number;
}

export interface BilanDto {
  periodeDebut: string;
  periodeFin: string;
  totalActif: number;
  totalPassif: number;
  encaissementsTotaux: number;
  creancesClients: number;
  produitsScolaires: number;
  chargesPersonnel: number;
  resultatNet: number;
  tresorerieNette: number;
  actif: BilanLigneDto[];
  passif: BilanLigneDto[];
  resultat: BilanLigneDto[];
}

export interface PersonnelDto {
  id: number;
  nom: string;
  prenom: string;
  nomComplet: string;
  email: string | null;
  telephone: string | null;
  matricule: string | null;
  categorie: string;
  categorieLibelle: string;
  enseignantId: number | null;
  salaireMensuel: number;
  dateEmbauche: string | null;
  statut: string;
}

export interface PersonnelRequest {
  nom: string;
  prenom: string;
  email?: string | null;
  telephone?: string | null;
  matricule?: string | null;
  categorie: string;
  enseignantId?: number | null;
  salaireMensuel: number;
  dateEmbauche?: string | null;
  statut: string;
}

export interface PaiePersonnelLigneDto {
  personnelId: number;
  nomComplet: string;
  matricule: string | null;
  categorie: string;
  categorieLibelle: string;
  salaireMensuel: number;
  paye: boolean;
  paiementId: number | null;
  montantPaye: number | null;
  datePaiement: string | null;
  modePaiement: string | null;
}

export interface PaiementPersonnelRequest {
  personnelId: number;
  mois: string;
  montant: number;
  modePaiement: string;
  datePaiement?: string | null;
  reference?: string | null;
}

export interface NotificationComptableDto {
  id: number;
  type: string;
  titre: string;
  message: string;
  lu: boolean;
  paiementId: number | null;
  demandePaiementId: number | null;
  dateCreation: string;
}

export interface DemandePaiementDto {
  id: number;
  factureId: number;
  factureNumero: string;
  parentNom: string;
  parentTelephone: string;
  eleveNom: string;
  niveau: string;
  montant: number;
  datePaiement: string;
  modePaiement: string;
  reference: string | null;
  preuveReference: string | null;
  statut: string;
  motifRefus: string | null;
  paiementId: number | null;
  numeroRecu: string | null;
  agentNom: string | null;
  dateCreation: string;
  dateTraitement: string | null;
}

export interface PaiementRequest {
  factureId: number;
  montant: number;
  datePaiement?: string | null;
  modePaiement?: string | null;
  reference?: string | null;
}

export interface DashboardFinancierDto {
  encaisseAujourdhui: number;
  encaisseSemaine: number;
  encaisseMois: number;
  encaisseAnnee: number;
  creancesTotales: number;
  facturesImpayees: number;
  repartitionFrais: RepartitionFraisDto[];
  revenusMensuels: RevenuMensuelDto[];
}

export interface RepartitionFraisDto {
  typeFrais: string;
  libelle: string;
  montant: number;
}

export interface RevenuMensuelDto {
  mois: number;
  annee: number;
  montant: number;
}

export interface FraisScolaireDto {
  id: number;
  typeFrais: string;
  typeFraisLibelle: string;
  libelle: string;
  montant: number;
  niveauId: number | null;
  niveauNom: string | null;
  classeId: number | null;
  classeNom: string | null;
  jourEcheance: number;
  actif: boolean;
}

export interface FraisScolaireRequest {
  typeFrais: string;
  libelle: string;
  montant: number;
  niveauId?: number | null;
  classeId?: number | null;
  jourEcheance: number;
  actif: boolean;
}

export interface RemiseEleveDto {
  id: number;
  eleveId: number;
  eleveNom: string;
  typeRemise: string;
  typeRemiseLibelle: string;
  libelle: string | null;
  pourcentage: number | null;
  montantFixe: number | null;
  actif: boolean;
  dateDebut: string | null;
  dateFin: string | null;
}

export interface RemiseEleveRequest {
  eleveId: number;
  typeRemise: string;
  libelle?: string | null;
  pourcentage?: number | null;
  montantFixe?: number | null;
  actif: boolean;
  dateDebut?: string | null;
  dateFin?: string | null;
}

export interface DepenseScolaireDto {
  id: number;
  categorie: string;
  categorieLibelle: string;
  dateDepense: string;
  montant: number;
  description: string;
  justificatif: string | null;
  agentNom: string | null;
}

export interface DepenseScolaireRequest {
  categorie: string;
  dateDepense: string;
  montant: number;
  description: string;
  justificatif?: string | null;
}

export interface SoldeEleveDto {
  eleveId: number;
  eleveNom: string;
  classe: string;
  niveau: string;
  montantBrut: number;
  montantRemise: number;
  montantPenalite: number;
  montantTotal: number;
  montantPaye: number;
  resteAPayer: number;
  statut: string;
  facturesImpayees: number;
  joursRetardMax: number;
  echeances: EcheanceMensuelleDto[];
  historiqueRetards: HistoriqueRetardDto[];
}

export interface EcheanceMensuelleDto {
  id: number;
  libelle: string;
  mois: string;
  montant: number;
  dateEcheance: string;
  statut: string;
  joursRetard: number;
}

export interface HistoriqueRetardDto {
  id: number;
  libelleEcheance: string;
  dateEcheance: string;
  joursRetard: number;
  montantPenalite: number;
  dateConstat: string;
}

export interface RapportFinancierDto {
  type: string;
  periodeDebut: string;
  periodeFin: string;
  totalEncaissements: number;
  totalDepenses: number;
  resultatNet: number;
  paiements: PaiementHistoriqueDto[];
  depenses: DepenseScolaireDto[];
}
