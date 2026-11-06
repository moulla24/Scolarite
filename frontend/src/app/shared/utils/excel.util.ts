import { GrilleNotesDto } from '../../core/api/api-dto';
import { downloadImportTemplate, exportTableToExcel, parseExcelRows, cellStr, cellNum, cellDate } from './excel-core.util';

const NOTES_IMPORT_HEADERS = [
  'matricule',
  'matiere',
  'type_evaluation',
  'date_evaluation',
  'note_max',
  'note_obtenue',
] as const;

export interface NotesImportRow {
  matricule: string;
  matiere: string;
  typeEvaluation: string;
  dateEvaluation: string;
  noteMax: number;
  noteObtenue: number;
}

export function downloadNotesImportTemplate(): void {
  downloadImportTemplate({
    filename: 'modele-import-notes.xlsx',
    sheetName: 'Import notes',
    instructions: [
      'matricule : identifiant élève (ex. MAT-2024-001).',
      'date_evaluation : format AAAA-MM-JJ.',
      'note_obtenue : nombre décimal (virgule ou point).',
    ],
    headers: [...NOTES_IMPORT_HEADERS],
    examples: [
      ['MAT-2024-001', 'Français', 'Contrôle', '2025-10-15', '20', '15.5'],
      ['MAT-2024-002', 'Mathématiques', 'Devoir', '2025-11-01', '20', '14'],
    ],
    colWidths: [16, 18, 14, 14, 10, 12],
  });
}

export function exportSummaryToExcel(
  rows: readonly { nomComplet: string; classe: string; nombreMatieres: number }[],
  filename: string,
  meta?: { niveau?: string; classe?: string }
): void {
  const metaRows: (readonly [string, string | number])[] = [];
  if (meta?.niveau) metaRows.push(['Niveau', meta.niveau]);
  if (meta?.classe) metaRows.push(['Classe', meta.classe]);
  exportTableToExcel({
    filename,
    sheetName: 'Synthèse notes',
    title: 'Synthèse des notes par élève',
    meta: metaRows,
    headers: ['Élève', 'Classe', 'Matières notées'],
    rows: rows.map((r) => [r.nomComplet, r.classe, r.nombreMatieres]),
    colWidths: [28, 18, 16],
  });
}

export function exportGrilleToExcel(
  grille: GrilleNotesDto,
  filename: string,
  meta?: { niveau?: string; classe?: string; matiere?: string }
): void {
  const metaRows: (readonly [string, string | number])[] = [];
  if (meta?.niveau) metaRows.push(['Niveau', meta.niveau]);
  if (meta?.classe) metaRows.push(['Classe', meta.classe]);
  if (meta?.matiere) metaRows.push(['Matière', meta.matiere]);
  exportTableToExcel({
    filename,
    sheetName: 'Notes',
    title: 'Grille des notes',
    meta: metaRows,
    headers: ['Élève', 'Classe', ...grille.colonnes.map((c) => c.libelle)],
    rows: grille.lignes.map((l) => [
      l.nomComplet,
      l.classe,
      ...l.notes.map((n) => (n != null ? n : '')),
    ]),
    colWidths: [28, 18, ...grille.colonnes.map(() => 22)],
  });
}

export function exportBulletinToExcel(
  nomComplet: string,
  classe: string,
  lignes: readonly { matiere: string; type: string; dateEvaluation: string; noteMax: number; noteObtenue: number }[],
  filename: string
): void {
  exportTableToExcel({
    filename,
    sheetName: 'Bulletin',
    title: 'Bulletin scolaire',
    meta: [
      ['Élève', nomComplet],
      ['Classe', classe],
    ],
    headers: ['Matière', 'Type', 'Date', 'Note max', 'Note obtenue'],
    rows: lignes.map((l) => [l.matiere, l.type, l.dateEvaluation, l.noteMax, l.noteObtenue]),
    colWidths: [22, 14, 14, 10, 14],
  });
}

export async function parseNotesImportExcel(file: File): Promise<NotesImportRow[]> {
  const rows = await parseExcelRows(file, { expectedHeaders: NOTES_IMPORT_HEADERS });

  return rows
    .map((row) => {
      const matricule = cellStr(row, 'matricule', 'Matricule');
      const matiere = cellStr(row, 'matiere', 'Matière', 'Matiere');
      const typeEvaluation = cellStr(row, 'type_evaluation', 'typeEvaluation', 'Type');
      const dateEvaluation = cellDate(row, 'date_evaluation', 'dateEvaluation', 'Date');
      const noteMax = cellNum(row, 'note_max', 'noteMax', 'Note max') || 20;
      const noteObtenue = cellNum(
        row,
        'note_obtenue',
        'noteObtenue',
        'Note',
        'note_obtenue'
      );

      return { matricule, matiere, typeEvaluation, dateEvaluation, noteMax, noteObtenue };
    })
    .filter(
      (r) =>
        r.matricule &&
        r.matiere &&
        r.typeEvaluation &&
        r.dateEvaluation &&
        !Number.isNaN(r.noteMax) &&
        !Number.isNaN(r.noteObtenue)
    );
}

export * from './excel-entities.util';
export * from './excel-import.util';
export * from './excel-core.util';
