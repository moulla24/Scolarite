import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { EnseignantDto } from '../../../../core/api/api-dto';
import { ReferentielApiService } from '../../../../core/api/referentiel-api.service';
import { toClassFilters } from '../../../../core/api/api-mappers';
import { FilterOption } from '../../../../core/models/filter-option.model';
import { environment } from '../../../../../environments/environment';
import { AssignmentsApiService } from '../../services/assignments-api.service';

export interface AssignmentFormModel {
  classeId: string;
  matiereId: string;
  enseignantId: string;
}

const emptyForm = (): AssignmentFormModel => ({
  classeId: '',
  matiereId: '',
  enseignantId: '',
});

@Component({
  selector: 'app-assignment-form-panel',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './assignment-form-panel.component.html',
  styleUrls: [
    '../../../students/components/student-create-panel/student-create-panel.component.scss',
    './assignment-form-panel.component.scss',
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AssignmentFormPanelComponent implements OnInit {
  private readonly api = inject(AssignmentsApiService);
  private readonly referentiel = inject(ReferentielApiService);
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  readonly editId = input<number | null>(null);

  readonly saved = output<void>();
  readonly cancelled = output<void>();

  readonly form = signal<AssignmentFormModel>(emptyForm());
  readonly classOptions = signal<readonly FilterOption[]>([]);
  readonly subjectOptions = signal<readonly FilterOption[]>([]);
  readonly teacherOptions = signal<readonly FilterOption[]>([]);
  readonly saving = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => {
      const id = this.editId();
      if (id != null) {
        this.loadForEdit(id);
      } else {
        this.form.set(emptyForm());
        this.loading.set(false);
      }
    });
  }

  ngOnInit(): void {
    this.referentiel.getClasses().subscribe({
      next: (c) => this.classOptions.set(toClassFilters(c).filter((x) => x.id !== 'all')),
    });
    this.referentiel.getMatieres().subscribe({
      next: (m) =>
        this.subjectOptions.set(m.map((x) => ({ id: String(x.id), label: x.nom }))),
    });
    this.http.get<EnseignantDto[]>(`${this.base}/enseignants`).subscribe({
      next: (t) =>
        this.teacherOptions.set(
          t.map((x) => ({ id: String(x.id), label: x.nomComplet }))
        ),
    });
  }

  readonly isEditMode = () => this.editId() != null;

  patchField(field: keyof AssignmentFormModel, value: string): void {
    this.form.update((f) => ({ ...f, [field]: value }));
  }

  onSubmit(): void {
    const f = this.form();
    if (!f.classeId || !f.matiereId || !f.enseignantId) {
      this.errorMessage.set('Classe, matière et enseignant sont obligatoires.');
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const body = {
      classeId: Number(f.classeId),
      matiereId: Number(f.matiereId),
      enseignantId: Number(f.enseignantId),
    };

    const id = this.editId();
    const req$ = id != null ? this.api.update(id, body) : this.api.create(body);

    req$.subscribe({
      next: () => {
        this.saving.set(false);
        this.form.set(emptyForm());
        this.saved.emit();
      },
      error: (err: { error?: { message?: string } }) => {
        this.saving.set(false);
        this.errorMessage.set(
          err?.error?.message ?? 'Impossible d’enregistrer l’affectation. Vérifiez les champs.'
        );
      },
    });
  }

  onCancel(): void {
    this.form.set(emptyForm());
    this.errorMessage.set(null);
    this.cancelled.emit();
  }

  private loadForEdit(id: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.api.getById(id).subscribe({
      next: (dto) => {
        this.form.set({
          classeId: String(dto.classeId),
          matiereId: String(dto.matiereId),
          enseignantId: String(dto.enseignantId),
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger l’affectation.');
      },
    });
  }
}
