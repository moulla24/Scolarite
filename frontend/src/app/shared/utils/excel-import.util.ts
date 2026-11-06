import { Observable, catchError, forkJoin, map, of } from 'rxjs';
import { firstValueFrom } from 'rxjs';

export interface ImportBatchResult {
  success: number;
  errors: number;
  total: number;
}

export async function runImportBatch<T>(
  rows: readonly T[],
  importer: (row: T) => Observable<unknown>
): Promise<ImportBatchResult> {
  if (rows.length === 0) {
    return { success: 0, errors: 0, total: 0 };
  }
  return firstValueFrom(
    forkJoin(
      rows.map((row) =>
        importer(row).pipe(
          map(() => true),
          catchError(() => of(false))
        )
      )
    ).pipe(
      map((results) => ({
        success: results.filter(Boolean).length,
        errors: results.filter((r) => !r).length,
        total: rows.length,
      }))
    )
  );
}

export function formatImportResult(result: ImportBatchResult): string {
  if (result.total === 0) return 'Aucune ligne valide à importer.';
  return `${result.success} ligne(s) importée(s), ${result.errors} erreur(s) sur ${result.total}.`;
}

export const EXCEL_ACCEPT = '.xlsx,.xls,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
