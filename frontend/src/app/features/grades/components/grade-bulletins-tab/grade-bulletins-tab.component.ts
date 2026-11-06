import { ChangeDetectionStrategy, Component, inject, input, OnChanges, signal, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { BulletinEleveStatutDto } from '../../../../core/api/api-dto';
import { GradesApiService } from '../../services/grades-api.service';

interface DownloadSelection {
  trimestre: string;
  format: string;
}

@Component({
  selector: 'app-grade-bulletins-tab',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './grade-bulletins-tab.component.html',
  styleUrl: './grade-bulletins-tab.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GradeBulletinsTabComponent implements OnChanges {
  private readonly gradesApi = inject(GradesApiService);

  readonly classeId = input.required<string>();
  readonly niveauId = input.required<string>();

  readonly rows = signal<readonly BulletinEleveStatutDto[]>([]);
  readonly loading = signal(false);
  readonly loadError = signal(false);
  readonly downloadingEleveId = signal<number | null>(null);
  readonly selections = signal<Record<number, DownloadSelection>>({});

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['classeId'] || changes['niveauId']) {
      this.selections.set({});
      this.loadStatuts();
    }
  }

  isAvailable(row: BulletinEleveStatutDto, trimestre: number): boolean {
    if (trimestre === 1) return row.trimestre1Disponible;
    if (trimestre === 2) return row.trimestre2Disponible;
    return row.trimestre3Disponible;
  }

  getSelection(eleveId: number): DownloadSelection {
    return this.selections()[eleveId] ?? { trimestre: '', format: '' };
  }

  onTrimestreChange(eleveId: number, trimestre: string): void {
    this.selections.update((current) => ({
      ...current,
      [eleveId]: { trimestre, format: '' },
    }));
  }

  onFormatChange(eleveId: number, format: string): void {
    const trimestre = this.getSelection(eleveId).trimestre;
    this.selections.update((current) => ({
      ...current,
      [eleveId]: { trimestre, format },
    }));
  }

  canDownload(row: BulletinEleveStatutDto): boolean {
    const sel = this.getSelection(row.eleveId);
    if (!sel.trimestre || !sel.format) return false;
    return this.isAvailable(row, Number(sel.trimestre));
  }

  isDownloading(eleveId: number): boolean {
    return this.downloadingEleveId() === eleveId;
  }

  async download(row: BulletinEleveStatutDto): Promise<void> {
    const sel = this.getSelection(row.eleveId);
    const trimestre = Number(sel.trimestre);
    const format = sel.format as 'pdf' | 'excel';
    if (!this.canDownload(row)) return;

    this.downloadingEleveId.set(row.eleveId);
    try {
      const blob = await firstValueFrom(
        format === 'pdf'
          ? this.gradesApi.downloadBulletinPdf(row.eleveId, trimestre)
          : this.gradesApi.downloadBulletinExcel(row.eleveId, trimestre)
      );
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = `bulletin-T${trimestre}-${row.eleveId}.${format === 'pdf' ? 'pdf' : 'xlsx'}`;
      anchor.click();
      URL.revokeObjectURL(url);
    } catch {
      alert(
        'Téléchargement impossible. Le bulletin n’est disponible que lorsque toutes les matières ont un devoir et une composition saisis pour cet élève.'
      );
    } finally {
      this.downloadingEleveId.set(null);
    }
  }

  private async loadStatuts(): Promise<void> {
    const classeId = this.classeId();
    if (!classeId || classeId === 'all') {
      this.rows.set([]);
      return;
    }

    this.loading.set(true);
    this.loadError.set(false);
    try {
      const niveauId = this.niveauId();
      const data = await firstValueFrom(
        this.gradesApi.listBulletinStatuts(
          classeId,
          niveauId && niveauId !== 'all' ? niveauId : undefined
        )
      );
      this.rows.set(data);
      this.loading.set(false);
    } catch {
      this.rows.set([]);
      this.loading.set(false);
      this.loadError.set(true);
    }
  }
}
