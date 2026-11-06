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
import { splitNomComplet } from '../../../../shared/utils/nom-complet.util';
import { ComptablesApiService } from '../../services/comptables-api.service';
import {
  emptyComptableCreateForm,
  ComptableCreateFormModel,
} from '../../models/comptable-create.model';

@Component({
  selector: 'app-comptable-create-panel',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './comptable-create-panel.component.html',
  styleUrl: '../../../students/components/student-create-panel/student-create-panel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComptableCreatePanelComponent {
  private readonly api = inject(ComptablesApiService);

  readonly editId = input<number | null>(null);
  readonly saved = output<void>();
  readonly cancelled = output<void>();

  readonly form = signal<ComptableCreateFormModel>(emptyComptableCreateForm());
  readonly saving = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => {
      const id = this.editId();
      if (id != null) {
        this.loadForEdit(id);
      } else {
        this.form.set(emptyComptableCreateForm());
        this.loading.set(false);
      }
    });
  }

  readonly isEditMode = () => this.editId() != null;

  patchField(field: keyof ComptableCreateFormModel, value: string | boolean): void {
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
      nom: f.nom.trim(),
      prenom: f.prenom.trim(),
      email: f.email.trim(),
      telephone: f.telephone.trim() || null,
      matricule: f.matricule.trim() || null,
      dateEmbauche: f.dateEmbauche || null,
      actif: f.actif,
    };

    const id = this.editId();
    const req$ = id != null ? this.api.update(id, body) : this.api.create(body);

    req$.subscribe({
      next: () => {
        this.saving.set(false);
        this.form.set(emptyComptableCreateForm());
        this.saved.emit();
      },
      error: (err: { error?: { message?: string } }) => {
        this.saving.set(false);
        this.errorMessage.set(
          err?.error?.message ?? 'Impossible d’enregistrer le comptable.'
        );
      },
    });
  }

  onCancel(): void {
    this.form.set(emptyComptableCreateForm());
    this.errorMessage.set(null);
    this.cancelled.emit();
  }

  private loadForEdit(id: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.api.getById(id).subscribe({
      next: (dto) => {
        const { prenom, nom } = splitNomComplet(dto.nomComplet);
        this.form.set({
          nom,
          prenom,
          email: dto.email ?? '',
          telephone: dto.telephone ?? '',
          matricule: dto.matricule ?? '',
          dateEmbauche: dto.dateEmbauche ?? '',
          actif: dto.statut === 'ACTIF',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger le comptable.');
      },
    });
  }

  private validate(f: ComptableCreateFormModel): string | null {
    if (!f.nom.trim()) return 'Le nom est obligatoire.';
    if (!f.prenom.trim()) return 'Le prénom est obligatoire.';
    if (!f.email.trim()) return 'L’e-mail est obligatoire.';
    return null;
  }
}
