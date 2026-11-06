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
import { FilterOption } from '../../../../core/models/filter-option.model';
import { ReferentielApiService } from '../../../../core/api/referentiel-api.service';
import {
  emptyClassroomCreateForm,
  ClassroomCreateFormModel,
} from '../../models/classroom-create.model';
import { ClassroomsApiService } from '../../services/classrooms-api.service';

@Component({
  selector: 'app-classroom-create-panel',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './classroom-create-panel.component.html',
  styleUrl: '../../../students/components/student-create-panel/student-create-panel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClassroomCreatePanelComponent implements OnInit {
  private readonly api = inject(ClassroomsApiService);
  private readonly referentiel = inject(ReferentielApiService);

  readonly editId = input<number | null>(null);

  readonly saved = output<void>();
  readonly cancelled = output<void>();

  readonly form = signal<ClassroomCreateFormModel>(emptyClassroomCreateForm());
  readonly niveauOptions = signal<readonly FilterOption[]>([]);
  readonly saving = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => {
      const id = this.editId();
      if (id != null) {
        this.loadForEdit(id);
      } else {
        this.form.set(emptyClassroomCreateForm());
        this.loading.set(false);
      }
    });
  }

  ngOnInit(): void {
    this.referentiel.getNiveaux().subscribe({
      next: (niveaux) =>
        this.niveauOptions.set(
          niveaux.map((n) => ({ id: String(n.id), label: n.nom }))
        ),
    });
  }

  readonly isEditMode = () => this.editId() != null;

  patchField(field: keyof ClassroomCreateFormModel, value: string): void {
    this.form.update((f) => ({ ...f, [field]: value }));
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
      niveauId: Number(f.niveauId),
      nom: f.nom.trim(),
      salle: f.salle.trim() || null,
      capacite: Number(f.capacite),
    };

    const id = this.editId();
    const req$ = id != null ? this.api.update(id, body) : this.api.create(body);

    req$.subscribe({
      next: () => {
        this.saving.set(false);
        this.form.set(emptyClassroomCreateForm());
        this.saved.emit();
      },
      error: (err: { error?: { message?: string } }) => {
        this.saving.set(false);
        this.errorMessage.set(
          err?.error?.message ?? 'Impossible d’enregistrer la classe. Vérifiez les champs.'
        );
      },
    });
  }

  onCancel(): void {
    this.form.set(emptyClassroomCreateForm());
    this.errorMessage.set(null);
    this.cancelled.emit();
  }

  private loadForEdit(id: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.api.getById(id).subscribe({
      next: (dto) => {
        const niveauId =
          this.niveauOptions().find((o) => o.label === dto.niveau)?.id ?? '';
        this.form.set({
          niveauId,
          nom: dto.nom,
          salle: dto.salle ?? '',
          capacite: String(dto.capacite),
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger la classe.');
      },
    });
  }

  private validate(f: ClassroomCreateFormModel): string | null {
    if (!f.niveauId) return 'Veuillez sélectionner un niveau.';
    if (!f.nom.trim()) return 'Le nom de la classe est obligatoire.';
    const cap = Number(f.capacite);
    if (!f.capacite || isNaN(cap) || cap <= 0) return 'La capacité doit être un nombre positif.';
    return null;
  }
}
