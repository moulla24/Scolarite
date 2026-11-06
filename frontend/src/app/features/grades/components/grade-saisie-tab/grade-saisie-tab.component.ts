import { ChangeDetectionStrategy, Component, inject, input, OnChanges, output, signal, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { SaisieColonneDto, SaisieLigneDto, SaisieNotesDto } from '../../../../core/api/api-dto';
import { GradesApiService } from '../../services/grades-api.service';

interface EditableRow {
  eleveId: number;
  nomComplet: string;
  notes: Record<number, string>;
}

@Component({
  selector: 'app-grade-saisie-tab',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './grade-saisie-tab.component.html',
  styleUrl: './grade-saisie-tab.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GradeSaisieTabComponent implements OnChanges {
  private readonly gradesApi = inject(GradesApiService);

  readonly classeId = input.required<string>();
  readonly matiereId = input.required<string>();

  readonly saved = output<void>();

  readonly saisie = signal<SaisieNotesDto | null>(null);
  readonly colonnes = signal<readonly SaisieColonneDto[]>([]);
  readonly rows = signal<readonly EditableRow[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['classeId'] || changes['matiereId']) {
      this.loadSaisie();
    }
  }

  patchNote(eleveId: number, evaluationId: number, value: string): void {
    this.rows.update((current) =>
      current.map((row) =>
        row.eleveId === eleveId ? { ...row, notes: { ...row.notes, [evaluationId]: value } } : row
      )
    );
  }

  async onSave(): Promise<void> {
    const classeId = this.classeId();
    const matiereId = this.matiereId();
    if (!classeId || classeId === 'all' || !matiereId || matiereId === 'all') {
      this.errorMessage.set('Sélectionnez une classe et une matière.');
      return;
    }

    const payload = this.rows()
      .map((row) => ({
        eleveId: row.eleveId,
        notes: this.colonnes()
          .map((col) => ({
            evaluationId: col.evaluationId,
            noteObtenue: this.parseNote(row.notes[col.evaluationId] ?? ''),
          }))
          .filter((item) => item.noteObtenue != null),
      }))
      .filter((item) => item.notes.length > 0);

    if (payload.length === 0) {
      this.errorMessage.set('Saisissez au moins une note.');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    try {
      await firstValueFrom(
        this.gradesApi.saveSaisie({
          classeId: Number(classeId),
          matiereId: Number(matiereId),
          notes: payload,
        })
      );
      this.saving.set(false);
      this.saved.emit();
      await this.loadSaisie();
    } catch {
      this.saving.set(false);
      this.errorMessage.set('Impossible d’enregistrer les notes.');
    }
  }

  private async loadSaisie(): Promise<void> {
    const classeId = this.classeId();
    const matiereId = this.matiereId();
    if (!classeId || classeId === 'all' || !matiereId || matiereId === 'all') {
      this.saisie.set(null);
      this.colonnes.set([]);
      this.rows.set([]);
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    try {
      const data = await firstValueFrom(this.gradesApi.getSaisie(classeId, matiereId));
      this.saisie.set(data);
      this.colonnes.set(data.colonnes ?? []);
      this.rows.set(data.lignes.map((ligne) => this.toEditableRow(ligne, data.colonnes ?? [])));
      this.loading.set(false);
    } catch (err: unknown) {
      this.loading.set(false);
      this.saisie.set(null);
      this.colonnes.set([]);
      this.rows.set([]);
      const message =
        err && typeof err === 'object' && 'error' in err && typeof (err as { error?: { message?: string } }).error?.message === 'string'
          ? (err as { error: { message: string } }).error.message
          : 'Impossible de charger la grille de saisie. Vérifiez l’affectation classe/matière.';
      this.errorMessage.set(message);
    }
  }

  private toEditableRow(ligne: SaisieLigneDto, colonnes: readonly SaisieColonneDto[]): EditableRow {
    const notes: Record<number, string> = {};
    for (const col of colonnes) {
      const cell = ligne.notes.find((n) => n.evaluationId === col.evaluationId);
      notes[col.evaluationId] = cell?.noteObtenue != null ? String(cell.noteObtenue) : '';
    }
    return {
      eleveId: ligne.eleveId,
      nomComplet: ligne.nomComplet,
      notes,
    };
  }

  private parseNote(value: string): number | null {
    const trimmed = value.trim().replace(',', '.');
    if (!trimmed) return null;
    const n = Number(trimmed);
    return Number.isFinite(n) ? n : null;
  }
}
