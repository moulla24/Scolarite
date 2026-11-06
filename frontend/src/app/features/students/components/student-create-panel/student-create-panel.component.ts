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
import { FilterOption } from '../../../../core/models/filter-option.model';
import { ReferentielApiService } from '../../../../core/api/referentiel-api.service';
import { classFilterLabel } from '../../../../core/api/api-mappers';
import { ClasseDto } from '../../../../core/api/api-dto';
import {
  emptyStudentCreateForm,
  StudentCreateFormModel,
} from '../../models/student-create.model';
import { StudentsApiService } from '../../services/students-api.service';

@Component({
  selector: 'app-student-create-panel',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './student-create-panel.component.html',
  styleUrl: './student-create-panel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StudentCreatePanelComponent {
  private readonly api = inject(StudentsApiService);
  private readonly referentiel = inject(ReferentielApiService);

  readonly levelFilters = input.required<readonly FilterOption[]>();
  readonly editId = input<number | null>(null);

  readonly saved = output<void>();
  readonly cancelled = output<void>();

  readonly form = signal<StudentCreateFormModel>(emptyStudentCreateForm());
  readonly classOptions = signal<readonly FilterOption[]>([]);
  readonly saving = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => {
      const id = this.editId();
      if (id != null) {
        this.loadForEdit(id);
      } else {
        this.form.set(emptyStudentCreateForm());
        this.classOptions.set([]);
        this.loading.set(false);
      }
    });
  }

  readonly isEditMode = () => this.editId() != null;

  patchField(field: keyof StudentCreateFormModel, value: string): void {
    this.form.update((f) => ({ ...f, [field]: value }));
  }

  onNiveauChange(niveauId: string): void {
    this.form.update((f) => ({ ...f, niveauId, classeId: '' }));
    this.loadClasses(niveauId);
  }

  onSubmit(): void {
    const f = this.form();
    const err = this.validate(f);
    if (err) {
      this.errorMessage.set(err);
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const body = {
      matricule: f.matricule.trim(),
      nom: f.nom.trim(),
      prenom: f.prenom.trim(),
      dateNaissance: f.dateNaissance || null,
      sexe: f.sexe || null,
      classeId: f.classeId ? Number(f.classeId) : null,
      ecoleId: this.api.defaultEcoleId,
    };

    const id = this.editId();
    const req$ = id != null ? this.api.update(id, body) : this.api.create(body);

    req$.subscribe({
      next: () => {
        this.saving.set(false);
        this.form.set(emptyStudentCreateForm());
        this.classOptions.set([]);
        this.saved.emit();
      },
      error: (err: { error?: { message?: string } }) => {
        this.saving.set(false);
        this.errorMessage.set(
          err?.error?.message ?? 'Impossible d’enregistrer l’élève. Vérifiez les champs.'
        );
      },
    });
  }

  onCancel(): void {
    this.form.set(emptyStudentCreateForm());
    this.classOptions.set([]);
    this.errorMessage.set(null);
    this.cancelled.emit();
  }

  private loadForEdit(id: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.api.getById(id).subscribe({
      next: (dto) => {
        const niveauId =
          this.levelFilters().find((o) => o.label === dto.niveau)?.id ?? 'all';

        this.form.set({
          matricule: dto.matricule,
          nom: dto.nom ?? '',
          prenom: dto.prenom ?? '',
          dateNaissance: dto.dateNaissance ?? '',
          sexe: dto.sexe ?? '',
          niveauId,
          classeId: '',
        });

        if (niveauId !== 'all') {
          this.referentiel.getClasses(niveauId).subscribe({
            next: (classes) => {
              const opts = this.toClassOptions(classes);
              this.classOptions.set(opts);
              const match = opts.find((o) => o.label === dto.classe || dto.classe.endsWith(o.label));
              this.form.update((f) => ({
                ...f,
                classeId: match?.id ?? opts.find((o) => dto.classe.includes(o.label))?.id ?? '',
              }));
              this.loading.set(false);
            },
            error: () => this.loading.set(false),
          });
        } else {
          this.loading.set(false);
        }
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger l’élève.');
      },
    });
  }

  private loadClasses(niveauId: string): void {
    if (niveauId === 'all') {
      this.classOptions.set([]);
      return;
    }
    this.referentiel.getClasses(niveauId).subscribe({
      next: (classes) => this.classOptions.set(this.toClassOptions(classes)),
    });
  }

  private toClassOptions(classes: readonly ClasseDto[]): readonly FilterOption[] {
    return classes.map((c) => ({
      id: String(c.id),
      label: classFilterLabel(c),
    }));
  }

  private validate(f: StudentCreateFormModel): string | null {
    if (!f.matricule.trim()) return 'Le matricule est obligatoire.';
    if (!f.nom.trim()) return 'Le nom est obligatoire.';
    if (!f.prenom.trim()) return 'Le prénom est obligatoire.';
    if (!f.classeId) return 'Veuillez sélectionner une classe.';
    if (f.niveauId === 'all') return 'Veuillez sélectionner un niveau.';
    return null;
  }
}
