import {
  AnneeScolaireDto,
  ClasseDto,
  EcoleDto,
  EleveDto,
  EnseignementDto,
  NiveauDto,
  SeanceEmploiDto,
} from '../../core/api/api-dto';
import { DashboardStat } from '../../core/models/dashboard-stats.model';
import { Parent } from '../../core/models/parent.model';
import { PedagogicalTemplate } from '../../core/models/pedagogical-template.model';
import { Student } from '../../core/models/student.model';
import { Teacher } from '../../core/models/teacher.model';
import { Transaction } from '../../core/models/transaction.model';
import { Classroom } from '../../core/models/classroom.model';
import { ScheduleSlot } from '../../core/models/schedule.model';
import {
  cellBool,
  cellDate,
  cellNum,
  cellStr,
  downloadImportMultiSheetTemplate,
  downloadImportTemplate,
  exportMultiSheetWorkbook,
  exportTableToExcel,
  parseExcelRows,
} from './excel-core.util';

const ID_HINT = 'Utilisez les identifiants numériques affichés dans l’application ou exportés depuis chaque liste.';

// ——— Parents ———

const PARENT_HEADERS = [
  'nom',
  'prenom',
  'telephone',
  'email',
  'adresse',
  'matricules_eleves',
] as const;

const PARENT_STUDENT_REF_HEADERS = ['matricule', 'nom', 'prenom', 'niveau', 'classe'] as const;

export interface ParentImportRow {
  nom: string;
  prenom: string | null;
  telephone: string;
  email: string | null;
  adresse: string | null;
  matricules: string[];
}

function parseMatriculesList(raw: string): string[] {
  return raw
    .split(/[;,\n]/)
    .map((part) => part.trim())
    .filter(Boolean);
}

type ParentTemplateEleve = Pick<EleveDto, 'matricule' | 'nom' | 'prenom' | 'niveau' | 'classe'>;

export function downloadParentsTemplate(eleves: readonly ParentTemplateEleve[] = []): void {
  const refRows =
    eleves.length > 0
      ? eleves.map((e) => [e.matricule, e.nom, e.prenom, e.niveau, e.classe])
      : [
          ['MAT-2024-010', 'Ould Med', 'Sidi', 'CP', 'CP1'],
          ['MAT-2024-011', 'Mint Ely', 'Khadijetou', 'CP', 'CP1'],
        ];

  const exampleMatricules =
    eleves.length >= 2
      ? `${eleves[0].matricule}; ${eleves[1].matricule}`
      : 'MAT-2024-010; MAT-2024-011';

  downloadImportMultiSheetTemplate({
    filename: 'modele-import-parents.xlsx',
    sheets: [
      {
        sheetName: 'Parents',
        instructions: [
          'Colonnes obligatoires : nom, telephone.',
          'matricules_eleves : matricules des enfants séparés par ; (voir feuille « Référence élèves »).',
          'L’import crée le parent et affecte automatiquement les élèves correspondants.',
        ],
        headers: [...PARENT_HEADERS],
        examples: [
          [
            'Ould Ahmed',
            'Mohamed',
            '+222 45 12 34 56',
            'parent@email.mr',
            'Nouakchott',
            exampleMatricules,
          ],
          ['Mint Salem', 'Fatimetou', '+222 46 23 45 67', '', '', ''],
        ],
        colWidths: [18, 14, 18, 22, 22, 28],
      },
      {
        sheetName: 'Référence élèves',
        instructions: [
          'Liste des élèves inscrits : copiez les matricules dans la colonne matricules_eleves.',
          'Le niveau et la classe sont indicatifs pour vous aider à choisir.',
        ],
        headers: [...PARENT_STUDENT_REF_HEADERS],
        examples: refRows,
        colWidths: [16, 16, 16, 12, 14],
      },
    ],
  });
}

export function exportParentsToExcel(parents: readonly Parent[]): void {
  exportTableToExcel({
    filename: 'parents.xlsx',
    sheetName: 'Parents',
    title: 'Liste des parents',
    headers: ['Nom complet', 'Téléphone', 'Email', 'Élèves', 'Solde (MRU)', 'Statut'],
    rows: parents.map((p) => [
      p.fullName,
      p.phone,
      p.email,
      p.students.map((s) => s.name).join(' ; '),
      p.balance,
      p.status === 'up_to_date' ? 'À jour' : 'Dette',
    ]),
  });
}

export async function parseParentsImportExcel(file: File): Promise<ParentImportRow[]> {
  const rows = await parseExcelRows(file, { expectedHeaders: PARENT_HEADERS });
  return rows
    .map((row) => ({
      nom: cellStr(row, 'nom', 'Nom'),
      prenom: cellStr(row, 'prenom', 'Prenom', 'Prénom') || null,
      telephone: cellStr(row, 'telephone', 'Telephone', 'Téléphone'),
      email: cellStr(row, 'email', 'Email') || null,
      adresse: cellStr(row, 'adresse', 'Adresse') || null,
      matricules: parseMatriculesList(
        cellStr(
          row,
          'matricules_eleves',
          'matricules',
          'Matricules élèves',
          'Matricules eleves',
          'Matricules'
        )
      ),
    }))
    .filter((r) => r.nom && r.telephone);
}

export function buildMatriculeToEleveIdMap(
  eleves: readonly Pick<EleveDto, 'id' | 'matricule'>[]
): Map<string, number> {
  return new Map(eleves.map((e) => [e.matricule.trim().toLowerCase(), e.id]));
}

export function resolveEleveIdsFromMatricules(
  matricules: readonly string[],
  matriculeToId: ReadonlyMap<string, number>
): { eleveIds: number[]; unknownMatricules: string[] } {
  const eleveIds: number[] = [];
  const unknownMatricules: string[] = [];
  for (const matricule of matricules) {
    const id = matriculeToId.get(matricule.trim().toLowerCase());
    if (id == null) {
      unknownMatricules.push(matricule);
      continue;
    }
    if (!eleveIds.includes(id)) {
      eleveIds.push(id);
    }
  }
  return { eleveIds, unknownMatricules };
}

// ——— Élèves ———

const STUDENT_HEADERS = ['matricule', 'nom', 'prenom', 'tuteur_id', 'classe_id', 'date_naissance', 'sexe'] as const;

export function downloadStudentsTemplate(): void {
  downloadImportTemplate({
    filename: 'modele-import-eleves.xlsx',
    sheetName: 'Élèves',
    instructions: ['Colonnes obligatoires : matricule, nom, prenom, tuteur_id, classe_id.', ID_HINT],
    headers: [...STUDENT_HEADERS],
    examples: [
      ['MAT-2024-010', 'Ould Med', 'Sidi', '1', '21', '2014-05-10', 'M'],
      ['MAT-2024-011', 'Mint Ely', 'Khadijetou', '2', '21', '', 'F'],
    ],
  });
}

export function exportStudentsToExcel(students: readonly Student[]): void {
  exportTableToExcel({
    filename: 'eleves.xlsx',
    sheetName: 'Élèves',
    title: 'Liste des élèves',
    headers: ['Nom complet', 'Matricule', 'Classe', 'Niveau', 'Solde (MRU)', 'Statut'],
    rows: students.map((s) => [
      s.fullName,
      s.matricule,
      s.className,
      s.level,
      s.balance,
      s.status === 'up_to_date' ? 'À jour' : 'Dette',
    ]),
  });
}

export async function parseStudentsImportExcel(file: File) {
  const rows = await parseExcelRows(file, { expectedHeaders: STUDENT_HEADERS });
  return rows
    .map((row) => ({
      matricule: cellStr(row, 'matricule', 'Matricule'),
      nom: cellStr(row, 'nom', 'Nom'),
      prenom: cellStr(row, 'prenom', 'Prenom', 'Prénom'),
      tuteurId: cellNum(row, 'tuteur_id', 'tuteurId', 'Tuteur ID'),
      classeId: cellNum(row, 'classe_id', 'classeId', 'Classe ID'),
      dateNaissance: cellDate(row, 'date_naissance', 'dateNaissance') || null,
      sexe: cellStr(row, 'sexe', 'Sexe') || null,
    }))
    .filter(
      (r) =>
        r.matricule &&
        r.nom &&
        r.prenom &&
        !Number.isNaN(r.tuteurId) &&
        r.tuteurId > 0 &&
        !Number.isNaN(r.classeId) &&
        r.classeId > 0
    );
}

// ——— Affectations ———

const ASSIGNMENT_HEADERS = ['classe_id', 'matiere_id', 'enseignant_id'] as const;

export function downloadAssignmentsTemplate(): void {
  downloadImportTemplate({
    filename: 'modele-import-affectations.xlsx',
    sheetName: 'Affectations',
    instructions: ['Une ligne = une matière assignée à une classe et un enseignant.', ID_HINT],
    headers: [...ASSIGNMENT_HEADERS],
    examples: [['21', '1', '1'], ['21', '3', '2']],
  });
}

export function exportAssignmentsToExcel(assignments: readonly EnseignementDto[]): void {
  exportTableToExcel({
    filename: 'affectations.xlsx',
    sheetName: 'Affectations',
    title: 'Affectations enseignants',
    headers: ['ID', 'Classe ID', 'Classe', 'Matière ID', 'Matière', 'Enseignant ID', 'Enseignant'],
    rows: assignments.map((a) => [
      a.id,
      a.classeId,
      a.classe,
      a.matiereId,
      a.matiere,
      a.enseignantId,
      a.enseignant,
    ]),
  });
}

export async function parseAssignmentsImportExcel(file: File) {
  const rows = await parseExcelRows(file, { expectedHeaders: ASSIGNMENT_HEADERS });
  return rows
    .map((row) => ({
      classeId: cellNum(row, 'classe_id', 'classeId', 'Classe ID'),
      matiereId: cellNum(row, 'matiere_id', 'matiereId', 'Matière ID', 'Matiere ID'),
      enseignantId: cellNum(row, 'enseignant_id', 'enseignantId', 'Enseignant ID'),
    }))
    .filter(
      (r) =>
        !Number.isNaN(r.classeId) &&
        r.classeId > 0 &&
        !Number.isNaN(r.matiereId) &&
        r.matiereId > 0 &&
        !Number.isNaN(r.enseignantId) &&
        r.enseignantId > 0
    );
}

// ——— Enseignants ———

const TEACHER_HEADERS = ['nom', 'prenom', 'email', 'telephone', 'specialite', 'actif'] as const;

export function downloadTeachersTemplate(): void {
  downloadImportTemplate({
    filename: 'modele-import-enseignants.xlsx',
    sheetName: 'Enseignants',
    instructions: ['Colonnes obligatoires : nom, prenom, email.', 'actif : oui/non'],
    headers: [...TEACHER_HEADERS],
    examples: [
      ['Mint Ely', 'Khadijetou', 'khadijetou@ecoli.mr', '+222 45 11 22 33', 'Français / Arabe', 'oui'],
      ['Ould Sidaty', 'Mohamed', 'm.sidaty@ecoli.mr', '', 'Mathématiques', 'oui'],
    ],
  });
}

export function exportTeachersToExcel(teachers: readonly Teacher[]): void {
  exportTableToExcel({
    filename: 'enseignants.xlsx',
    sheetName: 'Enseignants',
    title: 'Corps enseignant',
    headers: ['Nom complet', 'Email', 'Téléphone', 'Matières', 'Classes', 'Statut'],
    rows: teachers.map((t) => [
      t.fullName,
      t.email,
      t.phone,
      t.subjects.join(' ; '),
      t.classes.join(' ; '),
      t.status === 'active' ? 'Actif' : 'Congé',
    ]),
  });
}

export async function parseTeachersImportExcel(file: File) {
  const rows = await parseExcelRows(file, { expectedHeaders: TEACHER_HEADERS });
  return rows
    .map((row) => ({
      nom: cellStr(row, 'nom', 'Nom'),
      prenom: cellStr(row, 'prenom', 'Prenom', 'Prénom'),
      email: cellStr(row, 'email', 'Email'),
      telephone: cellStr(row, 'telephone', 'Telephone', 'Téléphone') || null,
      specialite: cellStr(row, 'specialite', 'Specialite', 'Spécialité') || null,
      actif: cellBool(row, 'actif', 'Actif'),
    }))
    .filter((r) => r.nom && r.prenom && r.email);
}

// ——— Classes ———

const CLASSROOM_HEADERS = ['niveau_id', 'nom', 'salle', 'capacite'] as const;

export function downloadClassroomsTemplate(): void {
  downloadImportTemplate({
    filename: 'modele-import-classes.xlsx',
    sheetName: 'Classes',
    instructions: ['Colonnes obligatoires : niveau_id, nom, capacite.', ID_HINT],
    headers: [...CLASSROOM_HEADERS],
    examples: [
      ['12', '1AS', 'Bât. B', '40'],
      ['11', '3AF', 'Bât. A', '35'],
    ],
  });
}

export function exportClassroomsToExcel(classrooms: readonly Classroom[]): void {
  exportTableToExcel({
    filename: 'classes.xlsx',
    sheetName: 'Classes',
    title: 'Liste des classes',
    headers: ['ID', 'Nom', 'Niveau', 'Enseignant réf.', 'Effectif', 'Capacité', 'Salle', 'Occupation %'],
    rows: classrooms.map((c) => [
      c.id,
      c.name,
      c.level,
      c.teacherName,
      c.studentCount,
      c.capacity,
      c.room,
      c.capacity > 0 ? Math.round((c.studentCount / c.capacity) * 100) : 0,
    ]),
  });
}

export function exportClassesDtoToExcel(classes: readonly ClasseDto[]): void {
  exportTableToExcel({
    filename: 'classes.xlsx',
    sheetName: 'Classes',
    title: 'Liste des classes',
    headers: ['ID', 'Nom', 'Niveau', 'Enseignant réf.', 'Effectif', 'Capacité', 'Salle', 'Occupation %'],
    rows: classes.map((c) => [c.id, c.nom, c.niveau, c.enseignantReferent, c.effectif, c.capacite, c.salle, c.tauxOccupation]),
  });
}

export async function parseClassroomsImportExcel(file: File) {
  const rows = await parseExcelRows(file, { expectedHeaders: CLASSROOM_HEADERS });
  return rows
    .map((row) => ({
      niveauId: cellNum(row, 'niveau_id', 'niveauId', 'Niveau ID'),
      nom: cellStr(row, 'nom', 'Nom'),
      salle: cellStr(row, 'salle', 'Salle') || null,
      capacite: cellNum(row, 'capacite', 'Capacite', 'Capacité'),
    }))
    .filter((r) => r.nom && !Number.isNaN(r.niveauId) && r.niveauId > 0 && !Number.isNaN(r.capacite) && r.capacite > 0);
}

// ——— Emploi du temps ———

const SCHEDULE_HEADERS = [
  'semaine_id',
  'classe_id',
  'matiere_id',
  'enseignant_id',
  'jour_semaine',
  'heure_debut',
  'heure_fin',
  'salle',
  'type',
] as const;

export function downloadScheduleTemplate(): void {
  downloadImportTemplate({
    filename: 'modele-import-emploi-du-temps.xlsx',
    sheetName: 'Emploi du temps',
    instructions: [
      'jour_semaine : 1=Lundi … 5=Vendredi.',
      'heure_debut / heure_fin : format HH:MM.',
      ID_HINT,
    ],
    headers: [...SCHEDULE_HEADERS],
    examples: [['1', '21', '1', '1', '08:00', '10:00', 'Bât. B', 'COURS']],
  });
}

export function exportScheduleSlotsToExcel(slots: readonly ScheduleSlot[]): void {
  exportTableToExcel({
    filename: 'emploi-du-temps.xlsx',
    sheetName: 'Emploi du temps',
    title: 'Emploi du temps',
    headers: ['ID', 'Jour', 'Heure début', 'Heure fin', 'Classe', 'Matière', 'Enseignant', 'Salle'],
    rows: slots.map((s) => [s.id, s.day, s.startTime, s.endTime, s.className, s.subject, s.teacherName, s.room]),
  });
}

export function exportScheduleToExcel(seances: readonly SeanceEmploiDto[]): void {
  exportTableToExcel({
    filename: 'emploi-du-temps.xlsx',
    sheetName: 'Emploi du temps',
    title: 'Emploi du temps',
    headers: ['ID', 'Jour', 'Horaire', 'Classe', 'Matière', 'Enseignant', 'Salle', 'Type'],
    rows: seances.map((s) => [s.id, s.jour, s.horaire, s.classe, s.matiere, s.enseignant, s.salle, 'COURS']),
  });
}

export async function parseScheduleImportExcel(file: File) {
  const rows = await parseExcelRows(file, { expectedHeaders: SCHEDULE_HEADERS });
  return rows
    .map((row) => ({
      semaineId: cellNum(row, 'semaine_id', 'semaineId', 'Semaine ID'),
      classeId: cellNum(row, 'classe_id', 'classeId'),
      matiereId: cellNum(row, 'matiere_id', 'matiereId'),
      enseignantId: cellNum(row, 'enseignant_id', 'enseignantId'),
      jourSemaine: cellNum(row, 'jour_semaine', 'jourSemaine', 'Jour'),
      heureDebut: cellStr(row, 'heure_debut', 'heureDebut', 'Heure début'),
      heureFin: cellStr(row, 'heure_fin', 'heureFin', 'Heure fin'),
      salle: cellStr(row, 'salle', 'Salle') || '—',
      type: cellStr(row, 'type', 'Type') || 'COURS',
    }))
    .filter(
      (r) =>
        !Number.isNaN(r.semaineId) &&
        r.semaineId > 0 &&
        !Number.isNaN(r.classeId) &&
        r.classeId > 0 &&
        !Number.isNaN(r.matiereId) &&
        r.matiereId > 0 &&
        !Number.isNaN(r.enseignantId) &&
        r.enseignantId > 0 &&
        !Number.isNaN(r.jourSemaine) &&
        r.jourSemaine >= 1 &&
        r.jourSemaine <= 5 &&
        r.heureDebut &&
        r.heureFin
    );
}

// ——— Maquettes (métadonnées) ———

const MAQUETTE_HEADERS = ['titre', 'matiere_id', 'niveau_id', 'description', 'format', 'nb_pages'] as const;

export function downloadMaquettesTemplate(): void {
  downloadImportTemplate({
    filename: 'modele-import-maquettes.xlsx',
    sheetName: 'Maquettes',
    instructions: [
      'Import des fiches descriptives uniquement (pas le fichier PDF).',
      'format : PDF, DOCX ou PPTX.',
      ID_HINT,
    ],
    headers: [...MAQUETTE_HEADERS],
    examples: [
      ['Progression Français 1AF', '1', '11', 'Objectifs trimestriels', 'PDF', '24'],
    ],
  });
}

export function exportMaquettesToExcel(templates: readonly PedagogicalTemplate[]): void {
  exportTableToExcel({
    filename: 'maquettes-pedagogiques.xlsx',
    sheetName: 'Maquettes',
    title: 'Maquettes pédagogiques',
    headers: ['Titre', 'Matière', 'Niveau', 'Format', 'Pages', 'Mise à jour', 'Description'],
    rows: templates.map((m) => [
      m.title,
      m.subject,
      m.level,
      m.format.toUpperCase(),
      m.pageCount,
      m.updatedAt,
      m.description,
    ]),
  });
}

export async function parseMaquettesImportExcel(file: File) {
  const rows = await parseExcelRows(file, { expectedHeaders: MAQUETTE_HEADERS });
  return rows
    .map((row) => ({
      titre: cellStr(row, 'titre', 'Titre'),
      matiereId: cellNum(row, 'matiere_id', 'matiereId'),
      niveauId: cellNum(row, 'niveau_id', 'niveauId'),
      description: cellStr(row, 'description', 'Description') || '',
      format: (cellStr(row, 'format', 'Format') || 'PDF').toUpperCase(),
      nbPages: cellNum(row, 'nb_pages', 'nbPages', 'Pages') || 1,
    }))
    .filter(
      (r) =>
        r.titre &&
        !Number.isNaN(r.matiereId) &&
        r.matiereId > 0 &&
        !Number.isNaN(r.niveauId) &&
        r.niveauId > 0
    );
}

// ——— Dashboard ———

export function exportDashboardToExcel(
  stats: readonly DashboardStat[],
  transactions: readonly Transaction[],
  levelLabel: string
): void {
  exportMultiSheetWorkbook('tableau-de-bord.xlsx', [
    {
      name: 'Indicateurs',
      options: {
        title: 'Tableau de bord — Indicateurs',
        meta: [['Niveau filtré', levelLabel]],
        headers: ['Indicateur', 'Valeur', 'Détail'],
        rows: stats.map((s) => [s.title, s.value, s.detail]),
      },
    },
    {
      name: 'Transactions',
      options: {
        title: 'Transactions récentes',
        headers: ['Référence', 'Parent', 'Nb élèves', 'Niveau', 'Montant', 'Date', 'Statut'],
        rows: transactions.map((t) => [
          t.reference,
          t.parentName,
          t.studentsCount,
          t.level,
          t.amount,
          t.date,
          t.status === 'paid' ? 'Payé' : t.status === 'pending' ? 'En attente' : 'En retard',
        ]),
      },
    },
  ]);
}

// ——— Paramètres ———

export function exportSettingsToExcel(
  niveaux: readonly NiveauDto[],
  annees: readonly AnneeScolaireDto[],
  ecoles: readonly EcoleDto[]
): void {
  exportMultiSheetWorkbook('parametres-ecoli.xlsx', [
    {
      name: 'Niveaux',
      options: {
        title: 'Niveaux scolaires',
        headers: ['ID', 'École ID', 'École', 'Nom'],
        rows: niveaux.map((n) => [n.id, n.ecoleId, n.ecoleNom, n.nom]),
      },
    },
    {
      name: 'Années scolaires',
      options: {
        title: 'Années scolaires',
        headers: ['ID', 'École ID', 'Nom', 'Début', 'Fin', 'Active'],
        rows: annees.map((a) => [a.id, a.ecoleId, a.nom, a.dateDebut, a.dateFin, a.active ? 'Oui' : 'Non']),
      },
    },
    {
      name: 'Écoles',
      options: {
        title: 'Écoles',
        headers: ['ID', 'Nom', 'Code', 'Adresse', 'Téléphone', 'Email'],
        rows: ecoles.map((e) => [e.id, e.nom, e.code, e.adresse, e.telephone, e.email]),
      },
    },
  ]);
}

export function downloadSettingsNiveauxTemplate(ecoleId: number): void {
  downloadImportTemplate({
    filename: 'modele-import-niveaux.xlsx',
    sheetName: 'Niveaux',
    instructions: [`ecole_id par défaut : ${ecoleId}`],
    headers: ['ecole_id', 'nom'],
    examples: [[String(ecoleId), 'Primaire'], [String(ecoleId), 'Collège']],
  });
}

export function downloadSettingsAnneesTemplate(ecoleId: number): void {
  downloadImportTemplate({
    filename: 'modele-import-annees.xlsx',
    sheetName: 'Années',
    instructions: [`ecole_id : ${ecoleId}`, 'active : oui/non'],
    headers: ['ecole_id', 'nom', 'date_debut', 'date_fin', 'active'],
    examples: [[String(ecoleId), '2025-2026', '2025-09-01', '2026-06-30', 'oui']],
  });
}

export async function parseNiveauxImportExcel(file: File) {
  const rows = await parseExcelRows(file, { expectedHeaders: ['ecole_id', 'nom'] });
  return rows
    .map((row) => ({
      ecoleId: cellNum(row, 'ecole_id', 'ecoleId'),
      nom: cellStr(row, 'nom', 'Nom'),
    }))
    .filter((r) => r.nom && !Number.isNaN(r.ecoleId) && r.ecoleId > 0);
}

export async function parseAnneesImportExcel(file: File) {
  const rows = await parseExcelRows(file, {
    expectedHeaders: ['ecole_id', 'nom', 'date_debut', 'date_fin', 'active'],
  });
  return rows
    .map((row) => ({
      ecoleId: cellNum(row, 'ecole_id', 'ecoleId'),
      nom: cellStr(row, 'nom', 'Nom'),
      dateDebut: cellDate(row, 'date_debut', 'dateDebut'),
      dateFin: cellDate(row, 'date_fin', 'dateFin'),
      active: cellBool(row, 'active', 'Active'),
    }))
    .filter(
      (r) =>
        r.nom &&
        r.dateDebut &&
        r.dateFin &&
        !Number.isNaN(r.ecoleId) &&
        r.ecoleId > 0
    );
}
