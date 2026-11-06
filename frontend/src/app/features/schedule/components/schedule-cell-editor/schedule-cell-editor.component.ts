import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EnseignementDto } from '../../../../core/api/api-dto';
import { FilterOption } from '../../../../core/models/filter-option.model';
import { AssignmentsApiService } from '../../../assignments/services/assignments-api.service';
import { ScheduleCellContext } from '../../models/schedule-cell-context.model';
import { ScheduleApiService } from '../../services/schedule-api.service';

@Component({
  selector: 'app-schedule-cell-editor',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './schedule-cell-editor.component.html',
  styleUrl: './schedule-cell-editor.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScheduleCellEditorComponent {
  private readonly api = inject(ScheduleApiService);
  private readonly assignmentsApi = inject(AssignmentsApiService);

  readonly context = input.required<ScheduleCellContext>();

  readonly saved = output<void>();
  readonly cancelled = output<void>();

  readonly matiereId = signal('');
  readonly enseignantId = signal('');
  readonly salle = signal('');
  readonly subjectOptions = signal<readonly FilterOption[]>([]);
  readonly teacherOptions = signal<readonly FilterOption[]>([]);
  readonly enseignements = signal<readonly EnseignementDto[]>([]);
  readonly loadingAssignments = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => {
      const ctx = this.context();
      const slot = ctx.slot;
      this.salle.set(slot?.room && slot.room !== '—' ? slot.room : '');
      this.errorMessage.set(null);
      this.loadEnseignements(ctx.classId, slot?.matiereId ?? '', slot?.enseignantId ?? '');
    });
  }

  readonly isEditMode = () => this.context().slot != null;

  readonly hasAssignments = () => this.enseignements().length > 0;

  onMatiereChange(matiereId: string): void {
    this.matiereId.set(matiereId);
    this.syncTeachersForMatiere(matiereId);
  }

  onSubmit(): void {
    const ctx = this.context();
    const matiereId = this.matiereId().trim();
    const enseignantId = this.enseignantId().trim();

    if (!matiereId) {
      this.errorMessage.set('Veuillez choisir une matière.');
      return;
    }
    if (!enseignantId) {
      this.errorMessage.set('Veuillez choisir un enseignant.');
      return;
    }

    const payload = {
      semaineId: ctx.semaineId,
      classeId: Number(ctx.classId),
      matiereId: Number(matiereId),
      enseignantId: Number(enseignantId),
      jourSemaine: ctx.dayIndex,
      heureDebut: ctx.startTime,
      heureFin: ctx.endTime,
      salle: this.salle().trim(),
      type: 'Cours',
    };

    this.saving.set(true);
    this.errorMessage.set(null);

    const slotId = ctx.slot?.id;
    const req$ =
      slotId != null ? this.api.update(Number(slotId), payload) : this.api.create(payload);

    req$.subscribe({
      next: () => {
        this.saving.set(false);
        this.saved.emit();
      },
      error: (err: { error?: { message?: string } }) => {
        this.saving.set(false);
        this.errorMessage.set(
          err?.error?.message ?? 'Impossible d’enregistrer ce créneau.'
        );
      },
    });
  }

  onDelete(): void {
    const slotId = this.context().slot?.id;
    if (slotId == null) return;
    if (!confirm('Supprimer ce créneau ?')) return;

    this.saving.set(true);
    this.errorMessage.set(null);
    this.api.delete(Number(slotId)).subscribe({
      next: () => {
        this.saving.set(false);
        this.saved.emit();
      },
      error: () => {
        this.saving.set(false);
        this.errorMessage.set('Impossible de supprimer ce créneau.');
      },
    });
  }

  onCancel(): void {
    this.cancelled.emit();
  }

  private loadEnseignements(
    classId: string,
    initialMatiereId: string,
    initialEnseignantId: string
  ): void {
    this.loadingAssignments.set(true);
    this.assignmentsApi.list(classId).subscribe({
      next: (list) => {
        this.enseignements.set(list);
        this.subjectOptions.set(this.uniqueSubjects(list));
        const matiereId =
          initialMatiereId && list.some((e) => String(e.matiereId) === initialMatiereId)
            ? initialMatiereId
            : (this.subjectOptions()[0]?.id ?? '');
        this.matiereId.set(matiereId);
        this.syncTeachersForMatiere(matiereId, initialEnseignantId);
        this.loadingAssignments.set(false);
      },
      error: () => {
        this.enseignements.set([]);
        this.subjectOptions.set([]);
        this.teacherOptions.set([]);
        this.matiereId.set('');
        this.enseignantId.set('');
        this.loadingAssignments.set(false);
        this.errorMessage.set('Impossible de charger les affectations de la classe.');
      },
    });
  }

  private uniqueSubjects(list: readonly EnseignementDto[]): readonly FilterOption[] {
    const seen = new Set<number>();
    const options: FilterOption[] = [];
    for (const e of list) {
      if (seen.has(e.matiereId)) continue;
      seen.add(e.matiereId);
      options.push({ id: String(e.matiereId), label: e.matiere });
    }
    return options.sort((a, b) => a.label.localeCompare(b.label, 'fr'));
  }

  private syncTeachersForMatiere(matiereId: string, preferredEnseignantId = ''): void {
    if (!matiereId) {
      this.teacherOptions.set([]);
      this.enseignantId.set('');
      return;
    }
    const teachers = this.uniqueTeachers(
      this.enseignements().filter((e) => String(e.matiereId) === matiereId)
    );
    this.teacherOptions.set(teachers);
    const enseignantId =
      preferredEnseignantId && teachers.some((t) => t.id === preferredEnseignantId)
        ? preferredEnseignantId
        : (teachers[0]?.id ?? '');
    this.enseignantId.set(enseignantId);
  }

  private uniqueTeachers(list: readonly EnseignementDto[]): readonly FilterOption[] {
    const seen = new Set<number>();
    const options: FilterOption[] = [];
    for (const e of list) {
      if (seen.has(e.enseignantId)) continue;
      seen.add(e.enseignantId);
      options.push({ id: String(e.enseignantId), label: e.enseignant });
    }
    return options.sort((a, b) => a.label.localeCompare(b.label, 'fr'));
  }
}
