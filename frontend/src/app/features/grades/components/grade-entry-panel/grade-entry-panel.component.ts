import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  output,
  signal,
  SimpleChanges,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { forkJoin, map, Observable, of, switchMap, throwError } from 'rxjs';
import { EleveDto, EnseignementDto, EvaluationDto } from '../../../../core/api/api-dto';
import { environment } from '../../../../../environments/environment';
import { GradesApiService } from '../../services/grades-api.service';

interface StudentNoteRow {
  eleveId: number;
  nomComplet: string;
  note: string;
  existingNoteId?: number;
}

@Component({
  selector: 'app-grade-entry-panel',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './grade-entry-panel.component.html',
  styleUrl: '../../../students/components/student-create-panel/student-create-panel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GradeEntryPanelComponent implements OnChanges {
  private readonly gradesApi = inject(GradesApiService);
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  readonly classeId = input.required<string>();
  readonly matiereFilterId = input<string>('all');

  readonly saved = output<void>();
  readonly cancelled = output<void>();

  readonly evaluations = signal<readonly EvaluationDto[]>([]);
  readonly studentRows = signal<readonly StudentNoteRow[]>([]);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly loading = signal(false);

  readonly useNewEvaluation = signal(false);
  readonly enseignements = signal<readonly EnseignementDto[]>([]);
  evaluationId = '';
  evalType = 'Contrôle';
  evalDate = new Date().toISOString().slice(0, 10);
  evalNoteMax = '20';
  enseignementId = '';
  newEvalType = 'Contrôle';
  newEvalDate = new Date().toISOString().slice(0, 10);
  newEvalNoteMax = '20';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['classeId']) {
      this.loadData();
    }
  }

  onCancel(): void {
    this.cancelled.emit();
  }

  onToggleNewEvaluation(useNew: boolean): void {
    this.useNewEvaluation.set(useNew);
  }

  patchStudentNote(eleveId: number, note: string): void {
    this.studentRows.update((rows) =>
      rows.map((r) => (r.eleveId === eleveId ? { ...r, note } : r))
    );
  }

  onSubmit(): void {
    const classeId = this.classeId();
    if (!classeId || classeId === 'all') {
      this.errorMessage.set('Sélectionnez une classe avant de saisir les notes.');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const resolveEvaluationId$: Observable<number> = this.useNewEvaluation()
      ? !this.enseignementId
        ? throwError(() => new Error('NO_ENSEIGNEMENT'))
        : this.gradesApi
          .createEvaluation({
            enseignementId: Number(this.enseignementId),
            type: this.newEvalType.trim() || 'Contrôle',
            dateEvaluation: this.newEvalDate,
            noteMax: Number(this.newEvalNoteMax) || 20,
          })
          .pipe(map((e) => e.id))
      : this.evaluationId
        ? of(Number(this.evaluationId))
        : throwError(() => new Error('NO_EVAL'));

    resolveEvaluationId$
      .pipe(
        switchMap((evalId) => {
          const rows = this.studentRows().filter((r) => r.note.trim() !== '');
          if (rows.length === 0) {
            throw new Error('NO_NOTES');
          }
          const requests = rows.map((r) => {
            const payload = {
              evaluationId: evalId,
              eleveId: r.eleveId,
              noteObtenue: Number(r.note.replace(',', '.')),
            };
            if (r.existingNoteId) {
              return this.gradesApi.updateNote(r.existingNoteId, payload);
            }
            return this.gradesApi.createNote(payload);
          });
          return forkJoin(requests);
        })
      )
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.saved.emit();
        },
        error: (err: Error & { message?: string }) => {
          this.saving.set(false);
          if (err?.message === 'NO_EVAL') {
            this.errorMessage.set('Choisissez une évaluation ou créez-en une nouvelle.');
            return;
          }
          if (err?.message === 'NO_ENSEIGNEMENT') {
            this.errorMessage.set('Sélectionnez un enseignement (classe / matière).');
            return;
          }
          if (err?.message === 'NO_NOTES') {
            this.errorMessage.set('Saisissez au moins une note.');
            return;
          }
          this.errorMessage.set('Impossible d’enregistrer les notes. Vérifiez les données.');
        },
      });
  }

  private loadData(): void {
    const classeId = this.classeId();
    if (!classeId || classeId === 'all') {
      this.evaluations.set([]);
      this.studentRows.set([]);
      return;
    }

    this.loading.set(true);
    this.gradesApi.listEnseignements(classeId, this.matiereFilterId()).subscribe({
      next: (ens) => {
        this.enseignements.set(ens);
        if (ens.length > 0 && !this.enseignementId) {
          this.enseignementId = String(ens[0].id);
        }
      },
    });
    this.http
      .get<EleveDto[]>(`${this.base}/eleves`, { params: { classeId } })
      .pipe(
        switchMap((eleves) =>
          this.gradesApi.listEvaluations(classeId).pipe(
            switchMap((evaluations) => {
              this.evaluations.set(evaluations);
              const matiereId = this.matiereFilterId();
              const filtered =
                matiereId && matiereId !== 'all'
                  ? evaluations.filter((e) => {
                      const matiereLabel = evaluations.find((x) => x.id === e.id)?.matiere;
                      return matiereLabel === e.matiere;
                    })
                  : evaluations;
              if (filtered.length > 0 && !this.evaluationId) {
                this.evaluationId = String(filtered[0].id);
              } else if (evaluations.length > 0 && !this.evaluationId) {
                this.evaluationId = String(evaluations[0].id);
              }
              const evalId = this.evaluationId ? Number(this.evaluationId) : null;
              if (!evalId) {
                return of(
                  eleves.map((e) => ({
                    eleveId: e.id,
                    nomComplet: e.nomComplet,
                    note: '',
                  }))
                );
              }
              return this.gradesApi.getNotesByEvaluation(evalId).pipe(
                switchMap((notes) =>
                  of(
                    eleves.map((e) => {
                      const existing = notes.find((n) => n.eleveId === e.id);
                      return {
                        eleveId: e.id,
                        nomComplet: e.nomComplet,
                        note: existing ? String(existing.noteObtenue) : '',
                        existingNoteId: existing?.id,
                      };
                    })
                  )
                )
              );
            })
          )
        )
      )
      .subscribe({
        next: (rows) => {
          this.studentRows.set(rows);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.errorMessage.set('Impossible de charger les élèves ou les évaluations.');
        },
      });
  }

  onEvaluationChange(id: string): void {
    this.evaluationId = id;
    this.useNewEvaluation.set(false);
    const evalId = Number(id);
    if (!evalId) return;
    this.gradesApi.getNotesByEvaluation(evalId).subscribe({
      next: (notes) => {
        this.studentRows.update((rows) =>
          rows.map((r) => {
            const existing = notes.find((n) => n.eleveId === r.eleveId);
            return {
              ...r,
              note: existing ? String(existing.noteObtenue) : '',
              existingNoteId: existing?.id,
            };
          })
        );
      },
    });
  }
}
