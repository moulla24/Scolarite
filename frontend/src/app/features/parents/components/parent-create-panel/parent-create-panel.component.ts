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
import { classFilterLabel } from '../../../../core/api/api-mappers';
import { ClasseDto, EleveDto, NiveauDto } from '../../../../core/api/api-dto';
import { ReferentielApiService } from '../../../../core/api/referentiel-api.service';
import { FilterOption } from '../../../../core/models/filter-option.model';
import {
  emptyParentCreateForm,
  ParentCreateFormModel,
} from '../../models/parent-create.model';
import { ParentEleveSelection } from '../../models/parent-eleve-selection.model';
import { ParentsApiService } from '../../services/parents-api.service';

@Component({
  selector: 'app-parent-create-panel',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './parent-create-panel.component.html',
  styleUrls: [
    '../../../students/components/student-create-panel/student-create-panel.component.scss',
    './parent-create-panel.component.scss',
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ParentCreatePanelComponent implements OnInit {
  private readonly api = inject(ParentsApiService);
  private readonly referentiel = inject(ReferentielApiService);

  readonly editId = input<number | null>(null);

  readonly saved = output<void>();
  readonly cancelled = output<void>();

  readonly form = signal<ParentCreateFormModel>(emptyParentCreateForm());
  readonly saving = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly niveauOptions = signal<readonly FilterOption[]>([]);
  readonly classOptions = signal<readonly FilterOption[]>([]);
  readonly filterNiveauId = signal('');
  readonly filterClasseId = signal('');
  readonly availableEleves = signal<readonly EleveDto[]>([]);
  readonly loadingEleves = signal(false);
  readonly selectedEleves = signal<readonly ParentEleveSelection[]>([]);

  constructor() {
    effect(() => {
      const id = this.editId();
      if (id != null) {
        this.loadForEdit(id);
      } else {
        this.resetForm();
      }
    });
  }

  ngOnInit(): void {
    this.referentiel.getNiveaux().subscribe({
      next: (niveaux) => this.niveauOptions.set(this.toNiveauOptions(niveaux)),
    });
  }

  readonly isEditMode = () => this.editId() != null;

  patchField(field: keyof ParentCreateFormModel, value: string): void {
    this.form.update((f) => ({ ...f, [field]: value }));
  }

  onFilterNiveauChange(niveauId: string): void {
    this.filterNiveauId.set(niveauId);
    this.filterClasseId.set('');
    this.availableEleves.set([]);
    this.loadClasses(niveauId);
  }

  onFilterClasseChange(classeId: string): void {
    this.filterClasseId.set(classeId);
    this.loadEleves();
  }

  isSelected(eleveId: number): boolean {
    return this.selectedEleves().some((e) => e.id === eleveId);
  }

  toggleEleve(eleve: EleveDto): void {
    if (this.isSelected(eleve.id)) {
      this.removeEleve(eleve.id);
      return;
    }
    this.selectedEleves.update((list) => [
      ...list,
      {
        id: eleve.id,
        nomComplet: eleve.nomComplet,
        niveau: eleve.niveau,
        classe: eleve.classe,
      },
    ]);
  }

  removeEleve(eleveId: number): void {
    this.selectedEleves.update((list) => list.filter((e) => e.id !== eleveId));
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
      prenom: f.prenom.trim() || null,
      email: f.email.trim() || null,
      telephone: f.telephone.trim(),
      adresse: f.adresse.trim() || null,
      eleveIds: this.selectedEleves().map((e) => e.id),
    };

    const id = this.editId();
    const req$ = id != null ? this.api.update(id, body) : this.api.create(body);

    req$.subscribe({
      next: () => {
        this.saving.set(false);
        this.resetForm();
        this.saved.emit();
      },
      error: (err: { error?: { message?: string } }) => {
        this.saving.set(false);
        this.errorMessage.set(
          err?.error?.message ?? 'Impossible d’enregistrer le parent. Vérifiez les champs.'
        );
      },
    });
  }

  onCancel(): void {
    this.resetForm();
    this.errorMessage.set(null);
    this.cancelled.emit();
  }

  private resetForm(): void {
    this.form.set(emptyParentCreateForm());
    this.selectedEleves.set([]);
    this.filterNiveauId.set('');
    this.filterClasseId.set('');
    this.classOptions.set([]);
    this.availableEleves.set([]);
    this.loading.set(false);
  }

  private loadForEdit(id: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.api.getById(id).subscribe({
      next: (dto) => {
        this.form.set({
          nom: dto.nom ?? '',
          prenom: dto.prenom ?? '',
          email: dto.email ?? '',
          telephone: dto.telephone ?? '',
          adresse: dto.adresse ?? '',
        });
        const affectes = dto.elevesAffectes ?? [];
        const selected =
          affectes.length > 0
            ? affectes.map((e) => ({
                id: e.id,
                nomComplet: e.nomComplet,
                niveau: e.niveau,
                classe: e.classe,
              }))
            : (dto.eleveIds ?? []).map((eleveId, index) => ({
                id: eleveId,
                nomComplet: dto.nomsEleves[index] ?? `Élève #${eleveId}`,
                niveau: '',
                classe: '',
              }));
        this.selectedEleves.set(selected);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger le parent.');
      },
    });
  }

  private loadClasses(niveauId: string): void {
    if (!niveauId) {
      this.classOptions.set([]);
      return;
    }
    this.referentiel.getClasses(niveauId).subscribe({
      next: (classes) => this.classOptions.set(this.toClassOptions(classes)),
    });
  }

  private loadEleves(): void {
    const niveauId = this.filterNiveauId();
    const classeId = this.filterClasseId();
    if (!niveauId || !classeId) {
      this.availableEleves.set([]);
      return;
    }
    this.loadingEleves.set(true);
    this.api.listEleves(niveauId, classeId).subscribe({
      next: (eleves) => {
        this.availableEleves.set(eleves);
        this.loadingEleves.set(false);
      },
      error: () => {
        this.availableEleves.set([]);
        this.loadingEleves.set(false);
      },
    });
  }

  private toNiveauOptions(niveaux: readonly NiveauDto[]): readonly FilterOption[] {
    return niveaux.map((n) => ({ id: String(n.id), label: n.nom }));
  }

  private toClassOptions(classes: readonly ClasseDto[]): readonly FilterOption[] {
    return classes.map((c) => ({
      id: String(c.id),
      label: classFilterLabel(c),
    }));
  }

  private validate(f: ParentCreateFormModel): string | null {
    if (!f.nom.trim()) return 'Le nom est obligatoire.';
    if (!f.telephone.trim()) return 'Le téléphone est obligatoire.';
    return null;
  }
}
