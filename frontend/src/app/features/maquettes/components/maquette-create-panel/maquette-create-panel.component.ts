import { ChangeDetectionStrategy, Component, inject, OnInit, output, signal } from '@angular/core';
import { switchMap } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { FilterOption } from '../../../../core/models/filter-option.model';
import { ReferentielApiService } from '../../../../core/api/referentiel-api.service';
import { toLevelFilters, toSubjectFilters } from '../../../../core/api/api-mappers';
import {
  emptyMaquetteCreateForm,
  MaquetteCreateFormModel,
} from '../../models/maquette-create.model';
import { MaquettesApiService } from '../../services/maquettes-api.service';

@Component({
  selector: 'app-maquette-create-panel',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './maquette-create-panel.component.html',
  styleUrl: '../../../students/components/student-create-panel/student-create-panel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MaquetteCreatePanelComponent implements OnInit {
  private readonly api = inject(MaquettesApiService);
  private readonly referentiel = inject(ReferentielApiService);

  readonly created = output<void>();
  readonly cancelled = output<void>();

  readonly form = signal<MaquetteCreateFormModel>(emptyMaquetteCreateForm());
  readonly subjectOptions = signal<readonly FilterOption[]>([]);
  readonly levelOptions = signal<readonly FilterOption[]>([]);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.referentiel.getMatieres().subscribe({
      next: (m) => this.subjectOptions.set(this.withoutAll(toSubjectFilters(m))),
    });
    this.referentiel.getNiveaux().subscribe({
      next: (n) => this.levelOptions.set(this.withoutAll(toLevelFilters(n))),
    });
  }

  patchField(field: keyof MaquetteCreateFormModel, value: string): void {
    this.form.update((f) => ({ ...f, [field]: value }));
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.form.update((f) => ({ ...f, fichier: file }));
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

    const nbPages = parseInt(f.nbPages, 10);
    const payload = {
      titre: f.titre.trim(),
      matiereId: Number(f.matiereId),
      niveauId: Number(f.niveauId),
      description: f.description.trim() || null,
      format: f.format,
      fichierUrl: f.fichierUrl.trim() || null,
      nbPages: Number.isNaN(nbPages) ? 0 : Math.max(0, nbPages),
      dateMaj: f.dateMaj || null,
    };

    const create$ = f.fichier
      ? this.api.uploadFile(f.fichier).pipe(
          switchMap((res) =>
            this.api.create({ ...payload, fichierUrl: res.url })
          )
        )
      : this.api.create(payload);

    create$.subscribe({
      next: () => {
        this.saving.set(false);
        this.form.set(emptyMaquetteCreateForm());
        this.created.emit();
      },
      error: (err: { error?: { message?: string } }) => {
        this.saving.set(false);
        this.errorMessage.set(
          err?.error?.message ?? 'Impossible d’enregistrer la maquette. Vérifiez les champs.'
        );
      },
    });
  }

  onCancel(): void {
    this.form.set(emptyMaquetteCreateForm());
    this.errorMessage.set(null);
    this.cancelled.emit();
  }

  private validate(f: MaquetteCreateFormModel): string | null {
    if (!f.titre.trim()) return 'Le titre est obligatoire.';
    if (!f.matiereId) return 'Veuillez sélectionner une matière.';
    if (!f.niveauId) return 'Veuillez sélectionner un niveau.';
    if (!f.format) return 'Veuillez sélectionner un format.';
    return null;
  }

  private withoutAll(options: readonly FilterOption[]): readonly FilterOption[] {
    return options.filter((o) => o.id !== 'all');
  }
}
