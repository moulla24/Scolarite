import * as XLSX from 'xlsx';

export interface ExcelTableExportOptions {
  filename: string;
  sheetName: string;
  title?: string;
  meta?: readonly (readonly [string, string | number])[];
  headers: readonly string[];
  rows: readonly (readonly (string | number | null | undefined)[])[];
  colWidths?: readonly number[];
}

export interface ExcelTemplateOptions {
  filename: string;
  sheetName: string;
  instructions?: readonly string[];
  headers: readonly string[];
  examples: readonly (readonly (string | number)[])[];
  colWidths?: readonly number[];
}

export function formatExportTimestamp(): string {
  return new Date().toLocaleString('fr-FR');
}

export function cellStr(row: Record<string, unknown>, ...keys: string[]): string {
  for (const key of keys) {
    const val = row[key];
    if (val == null || val === '') continue;
    return String(val).trim();
  }
  return '';
}

export function cellNum(row: Record<string, unknown>, ...keys: string[]): number {
  const raw = cellStr(row, ...keys).replace(',', '.');
  const n = Number(raw);
  return Number.isFinite(n) ? n : NaN;
}

export function cellDate(row: Record<string, unknown>, ...keys: string[]): string {
  for (const key of keys) {
    const val = row[key];
    if (val instanceof Date) return val.toISOString().slice(0, 10);
    const s = cellStr(row, key);
    if (s) return s.slice(0, 10);
  }
  return '';
}

export function cellBool(row: Record<string, unknown>, ...keys: string[]): boolean {
  const s = cellStr(row, ...keys).toLowerCase();
  return s === '1' || s === 'true' || s === 'oui' || s === 'yes' || s === 'actif';
}

export interface ParseExcelOptions {
  /** Colonnes attendues — permet de retrouver la ligne d'en-tête sous les instructions du modèle. */
  expectedHeaders?: readonly string[];
}

function normalizeHeader(cell: unknown): string {
  return String(cell ?? '')
    .trim()
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/\s+/g, '_');
}

function findHeaderRowIndex(aoa: unknown[][], expectedHeaders?: readonly string[]): number {
  const normalizedExpected = expectedHeaders?.map(normalizeHeader).filter(Boolean) ?? [];

  for (let i = 0; i < aoa.length; i++) {
    const row = aoa[i];
    if (!Array.isArray(row) || row.length === 0) continue;

    const cells = row.map(normalizeHeader).filter(Boolean);
    if (cells.length < 2) continue;

    if (normalizedExpected.length) {
      const matched = normalizedExpected.filter((h) => cells.includes(h)).length;
      const minMatch = Math.min(2, normalizedExpected.length);
      if (matched >= minMatch) return i;
      continue;
    }

    const first = cells[0] ?? '';
    if (first.startsWith('export') || first === 'exporte_le') continue;
    if (cells.length >= 3) return i;
  }

  return 0;
}

export async function parseExcelRows(
  file: File,
  options?: ParseExcelOptions
): Promise<Record<string, unknown>[]> {
  const buffer = await file.arrayBuffer();
  const wb = XLSX.read(buffer, { type: 'array', cellDates: true });
  const sheet = wb.Sheets[wb.SheetNames[0]];
  if (!sheet) return [];

  const aoa = XLSX.utils.sheet_to_json<unknown[]>(sheet, { header: 1, defval: '' });
  const headerRowIndex = findHeaderRowIndex(aoa, options?.expectedHeaders);
  const headers = (aoa[headerRowIndex] ?? []).map((h) => String(h ?? '').trim());

  const rows: Record<string, unknown>[] = [];
  for (let i = headerRowIndex + 1; i < aoa.length; i++) {
    const rowArr = aoa[i];
    if (!Array.isArray(rowArr) || rowArr.every((c) => c == null || c === '')) continue;

    const row: Record<string, unknown> = {};
    headers.forEach((header, j) => {
      if (header) row[header] = rowArr[j] ?? '';
    });
    rows.push(row);
  }
  return rows;
}

function buildSheetRows(options: {
  title?: string;
  meta?: readonly (readonly [string, string | number])[];
  instructions?: readonly string[];
  headers: readonly string[];
  rows: readonly (readonly (string | number | null | undefined)[])[];
  skipExportMeta?: boolean;
}): string[][] {
  const out: string[][] = [];
  if (!options.skipExportMeta) {
    if (options.title) out.push([options.title]);
    out.push(['Exporté le', formatExportTimestamp()]);
    if (options.meta?.length) {
      options.meta.forEach(([k, v]) => out.push([k, String(v)]));
    }
  }
  if (options.instructions?.length) {
    out.push([]);
    options.instructions.forEach((line) => out.push([line]));
  }
  if (out.length) out.push([]);
  out.push([...options.headers]);
  options.rows.forEach((row) => out.push(row.map((c) => (c == null ? '' : String(c)))));
  return out;
}

function normalizeFilename(filename: string): string {
  return filename.endsWith('.xlsx') ? filename : `${filename}.xlsx`;
}

/** Téléchargement fiable via lien temporaire (navigateur / Electron). */
export function saveWorkbook(wb: XLSX.WorkBook, filename: string): void {
  const name = normalizeFilename(filename);
  const data = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
  const blob = new Blob([data], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = name;
  anchor.style.display = 'none';
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
  URL.revokeObjectURL(url);
}

function applySheetLayout(ws: XLSX.WorkSheet, headerRowIndex: number, colCount: number): void {
  if (!ws['!ref']) return;
  const range = XLSX.utils.decode_range(ws['!ref']);
  if (range.e.r >= headerRowIndex) {
    ws['!autofilter'] = {
      ref: XLSX.utils.encode_range({ r: headerRowIndex, c: 0 }, { r: range.e.r, c: colCount - 1 }),
    };
  }
}

export function exportTableToExcel(options: ExcelTableExportOptions): void {
  const aoa = buildSheetRows({
    title: options.title,
    meta: options.meta,
    headers: options.headers,
    rows: options.rows,
  });
  const headerRowIndex = aoa.findIndex(
    (row, i) => row.length === options.headers.length && row[0] === options.headers[0] && i > 0
  );
  const ws = XLSX.utils.aoa_to_sheet(aoa);
  const widths =
    options.colWidths ??
    options.headers.map((h, i) => {
      const maxCell = Math.max(
        h.length,
        ...options.rows.map((r) => String(r[i] ?? '').length)
      );
      return Math.min(Math.max(maxCell + 2, 10), 40);
    });
  ws['!cols'] = widths.map((wch) => ({ wch }));
  applySheetLayout(ws, headerRowIndex >= 0 ? headerRowIndex : 0, options.headers.length);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, options.sheetName.slice(0, 31));
  saveWorkbook(wb, options.filename);
}

export interface ExcelMultiSheetTemplateOptions {
  filename: string;
  sheets: readonly {
    sheetName: string;
    instructions?: readonly string[];
    headers: readonly string[];
    examples: readonly (readonly (string | number)[])[];
    colWidths?: readonly number[];
  }[];
}

export function downloadImportMultiSheetTemplate(options: ExcelMultiSheetTemplateOptions): void {
  const wb = XLSX.utils.book_new();
  options.sheets.forEach((sheet) => {
    const aoa = buildSheetRows({
      instructions: sheet.instructions,
      headers: sheet.headers,
      rows: sheet.examples,
      skipExportMeta: true,
    });
    const headerRowIndex = aoa.findIndex(
      (row) => row.length === sheet.headers.length && row[0] === sheet.headers[0]
    );
    const ws = XLSX.utils.aoa_to_sheet(aoa);
    const widths =
      sheet.colWidths ??
      sheet.headers.map((h, i) => {
        const maxCell = Math.max(
          h.length,
          ...sheet.examples.map((r) => String(r[i] ?? '').length)
        );
        return Math.min(Math.max(maxCell + 2, 10), 40);
      });
    ws['!cols'] = widths.map((wch) => ({ wch }));
    applySheetLayout(ws, headerRowIndex >= 0 ? headerRowIndex : 0, sheet.headers.length);
    XLSX.utils.book_append_sheet(wb, ws, sheet.sheetName.slice(0, 31));
  });
  saveWorkbook(wb, options.filename);
}

export function downloadImportTemplate(options: ExcelTemplateOptions): void {
  const aoa = buildSheetRows({
    instructions: options.instructions,
    headers: options.headers,
    rows: options.examples,
    skipExportMeta: true,
  });
  const headerRowIndex = aoa.findIndex((row) => row.length === options.headers.length && row[0] === options.headers[0]);
  const ws = XLSX.utils.aoa_to_sheet(aoa);
  const widths =
    options.colWidths ??
    options.headers.map((h, i) => {
      const maxCell = Math.max(
        h.length,
        ...options.examples.map((r) => String(r[i] ?? '').length)
      );
      return Math.min(Math.max(maxCell + 2, 10), 36);
    });
  ws['!cols'] = widths.map((wch) => ({ wch }));
  applySheetLayout(ws, headerRowIndex >= 0 ? headerRowIndex : 0, options.headers.length);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, options.sheetName.slice(0, 31));
  saveWorkbook(wb, options.filename);
}

export function exportMultiSheetWorkbook(
  filename: string,
  sheets: readonly { name: string; options: Omit<ExcelTableExportOptions, 'filename' | 'sheetName'> }[]
): void {
  const wb = XLSX.utils.book_new();
  sheets.forEach((sheet) => {
    const aoa = buildSheetRows({
      title: sheet.options.title,
      meta: sheet.options.meta,
      headers: sheet.options.headers,
      rows: sheet.options.rows,
    });
    const headerRowIndex = aoa.findIndex(
      (row, i) => row.length === sheet.options.headers.length && row[0] === sheet.options.headers[0] && i > 0
    );
    const ws = XLSX.utils.aoa_to_sheet(aoa);
    const widths =
      sheet.options.colWidths ??
      sheet.options.headers.map((h, i) => {
        const maxCell = Math.max(
          h.length,
          ...sheet.options.rows.map((r) => String(r[i] ?? '').length)
        );
        return Math.min(Math.max(maxCell + 2, 10), 40);
      });
    ws['!cols'] = widths.map((wch) => ({ wch }));
    applySheetLayout(ws, headerRowIndex >= 0 ? headerRowIndex : 0, sheet.options.headers.length);
    XLSX.utils.book_append_sheet(wb, ws, sheet.name.slice(0, 31));
  });
  saveWorkbook(wb, filename);
}
