import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, Observable, finalize } from 'rxjs';
import {
  AnneeScolaireDto,
  NiveauDto,
  QuotaControleDto,
  TrimestreDto,
  TypeControleDto,
} from '../../../../core/api/api-dto';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { SettingsApiService } from '../../../settings/services/settings-api.service';
import { PlanificationApiService } from '../../services/planification-api.service';

type PlanificationTab = 'trimestres' | 'types' | 'quotas';

@Component({
  selector: 'app-planification-page',
  standalone: true,
  imports: [FormsModule, PageHeaderComponent],
  templateUrl: './planification-page.component.html',
  styleUrl: './planification-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlanificationPageComponent implements OnInit {
  private readonly api = inject(PlanificationApiService);
  private readonly settingsApi = inject(SettingsApiService);

  readonly activeTab = signal<PlanificationTab>('trimestres');
  readonly trimestres = signal<readonly TrimestreDto[]>([]);
  readonly trimestresQuota = signal<readonly TrimestreDto[]>([]);
  readonly typesControles = signal<readonly TypeControleDto[]>([]);
  readonly quotas = signal<readonly QuotaControleDto[]>([]);
  readonly annees = signal<readonly AnneeScolaireDto[]>([]);
  readonly niveaux = signal<readonly NiveauDto[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  editingTrimestreId: number | null = null;
  editingTypeId: number | null = null;

  trimAnneeId = '';
  trimNumero = '1';
  trimNom = '';
  trimDebut = '';
  trimFin = '';
  trimActif = true;

  typeNiveauId = '';
  typeCode = '';
  typeLibelle = '';
  typeNoteMax = '20';
  typeOrdre = '0';
  typeActif = true;

  quotaAnneeId = '';
  quotaTrimestreId = '';
  quotaNiveauId = '';

  ngOnInit(): void {
    this.loadInitialData();
  }

  setTab(tab: PlanificationTab): void {
    this.activeTab.set(tab);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.reloadActiveTab();
  }

  onQuotaContextChange(): void {
    this.reloadQuotas();
  }

  onTypeNiveauChange(): void {
    // Sélection du niveau dans le formulaire uniquement.
  }

  anneeCourante(): AnneeScolaireDto | undefined {
    return this.annees().find((a) => a.active);
  }

  reloadTrimestres(): void {
    const annee = this.anneeCourante();
    if (!annee) {
      this.trimestres.set([]);
      return;
    }
    this.fetch(
      this.api.listTrimestres(annee.id),
      (t) => this.trimestres.set(t),
      'Impossible de charger les trimestres.',
    );
  }

  reloadTrimestresQuota(): void {
    const annee = this.anneeCourante();
    if (!annee) {
      this.trimestresQuota.set([]);
      this.quotas.set([]);
      return;
    }
    this.quotaAnneeId = String(annee.id);
    this.fetch(
      this.api.listTrimestres(annee.id),
      (t) => {
        this.trimestresQuota.set(t);
        if (t.length > 0 && !this.quotaTrimestreId) {
          this.quotaTrimestreId = String(t[0].id);
        }
        this.reloadQuotas();
      },
      'Impossible de charger les trimestres.',
    );
  }

  reloadTypes(): void {
    this.fetch(
      this.api.listTypesControles(),
      (t) => this.typesControles.set(t),
      'Impossible de charger les types de contrôles.',
    );
  }

  reloadQuotas(): void {
    if (!this.quotaTrimestreId || !this.quotaNiveauId) {
      this.quotas.set([]);
      return;
    }
    this.fetch(
      this.api.listQuotasControles(Number(this.quotaTrimestreId), Number(this.quotaNiveauId)),
      (q) => this.quotas.set(q),
      'Impossible de charger les quotas.',
    );
  }

  onSubmitTrimestre(): void {
    const annee = this.anneeCourante();
    if (!annee) {
      this.errorMessage.set('Aucune année scolaire active.');
      return;
    }
    this.trimAnneeId = String(annee.id);
    if (!this.trimNom.trim() || !this.trimDebut || !this.trimFin) {
      this.errorMessage.set('Renseignez le nom et les dates du trimestre.');
      return;
    }
    const payload = {
      anneeScolaireId: Number(this.trimAnneeId),
      numero: Number(this.trimNumero),
      nom: this.trimNom.trim(),
      dateDebut: this.trimDebut,
      dateFin: this.trimFin,
      actif: this.trimActif,
    };
    this.saving.set(true);
    this.errorMessage.set(null);
    const request =
      this.editingTrimestreId != null
        ? this.api.updateTrimestre(this.editingTrimestreId, payload)
        : this.api.createTrimestre(payload);
    const wasEdit = this.editingTrimestreId != null;
    const anneeId = this.trimAnneeId;
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.resetTrimestreForm();
        this.successMessage.set(wasEdit ? 'Trimestre modifié.' : 'Trimestre créé.');
        this.reloadTrimestres();
        if (this.quotaAnneeId === anneeId) {
          this.reloadTrimestresQuota();
        }
      },
      error: (err) =>
        this.errorMessage.set(
          this.apiError(err, this.editingTrimestreId != null ? 'Modification impossible.' : 'Création impossible.'),
        ),
    });
  }

  onEditTrimestre(t: TrimestreDto): void {
    this.editingTrimestreId = t.id;
    this.trimAnneeId = String(t.anneeScolaireId);
    this.trimNumero = String(t.numero);
    this.trimNom = t.nom;
    this.trimDebut = t.dateDebut;
    this.trimFin = t.dateFin;
    this.trimActif = t.actif;
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  onCancelTrimestreEdit(): void {
    this.resetTrimestreForm();
  }

  onDeleteTrimestre(id: number): void {
    if (!confirm('Supprimer ce trimestre ?')) return;
    this.saving.set(true);
    this.api
      .deleteTrimestre(id)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          if (this.editingTrimestreId === id) {
            this.resetTrimestreForm();
          }
          if (String(id) === this.quotaTrimestreId) {
            this.quotaTrimestreId = '';
          }
          this.successMessage.set('Trimestre supprimé.');
          this.reloadTrimestres();
          this.reloadTrimestresQuota();
        },
        error: (err) => this.errorMessage.set(this.apiError(err, 'Suppression impossible.')),
      });
  }

  onSubmitType(): void {
    if (!this.typeNiveauId || !this.typeCode.trim() || !this.typeLibelle.trim()) {
      this.errorMessage.set('Niveau, code et libellé sont obligatoires.');
      return;
    }
    const noteMax = parseFloat(this.typeNoteMax);
    const ordre = parseInt(this.typeOrdre, 10);
    const payload = {
      niveauId: Number(this.typeNiveauId),
      code: this.typeCode.trim().toUpperCase(),
      libelle: this.typeLibelle.trim(),
      noteMax: Number.isNaN(noteMax) ? 20 : noteMax,
      ordre: Number.isNaN(ordre) ? 0 : ordre,
      actif: this.typeActif,
    };
    this.saving.set(true);
    this.errorMessage.set(null);
    const request =
      this.editingTypeId != null
        ? this.api.updateTypeControle(this.editingTypeId, payload)
        : this.api.createTypeControle(payload);
    const wasEdit = this.editingTypeId != null;
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.resetTypeForm();
        this.successMessage.set(wasEdit ? 'Type de contrôle modifié.' : 'Type de contrôle créé.');
        this.reloadTypes();
        this.reloadQuotas();
      },
      error: (err) =>
        this.errorMessage.set(
          this.apiError(err, this.editingTypeId != null ? 'Modification impossible.' : 'Création impossible.'),
        ),
    });
  }

  onEditType(t: TypeControleDto): void {
    this.editingTypeId = t.id;
    this.typeNiveauId = String(t.niveauId);
    this.typeCode = t.code;
    this.typeLibelle = t.libelle;
    this.typeNoteMax = String(t.noteMax);
    this.typeOrdre = String(t.ordre);
    this.typeActif = t.actif;
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  onCancelTypeEdit(): void {
    this.resetTypeForm();
  }

  onDeleteType(id: number): void {
    if (!confirm('Supprimer ce type de contrôle ?')) return;
    this.saving.set(true);
    this.api
      .deleteTypeControle(id)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          if (this.editingTypeId === id) {
            this.resetTypeForm();
          }
          this.successMessage.set('Type supprimé.');
          this.reloadTypes();
          this.reloadQuotas();
        },
        error: (err) => this.errorMessage.set(this.apiError(err, 'Suppression impossible.')),
      });
  }

  onSaveQuotas(): void {
    if (!this.quotaTrimestreId || !this.quotaNiveauId) {
      this.errorMessage.set('Sélectionnez un trimestre et un niveau.');
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    this.api
      .saveQuotasControles({
        trimestreId: Number(this.quotaTrimestreId),
        niveauId: Number(this.quotaNiveauId),
        quotas: this.quotas().map((q) => ({
          typeControleId: q.typeControleId,
          nombrePrevu: q.nombrePrevu,
        })),
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (saved) => {
          this.quotas.set(saved);
          this.successMessage.set('Quotas enregistrés.');
        },
        error: (err) => this.errorMessage.set(this.apiError(err, 'Enregistrement impossible.')),
      });
  }

  updateQuotaNombre(typeControleId: number, value: string): void {
    const parsed = parseInt(value, 10);
    const nombrePrevu = Number.isNaN(parsed) || parsed < 0 ? 0 : parsed;
    this.quotas.update((rows) =>
      rows.map((q) => (q.typeControleId === typeControleId ? { ...q, nombrePrevu } : q)),
    );
  }

  private loadInitialData(): void {
    this.loading.set(true);
    forkJoin({
      annees: this.settingsApi.listAnnees(),
      niveaux: this.settingsApi.listNiveaux(),
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ annees, niveaux }) => {
          const sorted = [...annees].sort((a, b) => {
            if (a.active !== b.active) return a.active ? -1 : 1;
            return a.nom.localeCompare(b.nom);
          });
          this.annees.set(sorted);
          this.niveaux.set(niveaux);

          const active = sorted.find((a) => a.active);
          if (active) {
            this.trimAnneeId = String(active.id);
            this.quotaAnneeId = String(active.id);
          }

          if (niveaux.length > 0) {
            this.typeNiveauId = String(niveaux[0].id);
            this.quotaNiveauId = String(niveaux[0].id);
          }

          this.reloadActiveTab();
        },
        error: (err) =>
          this.errorMessage.set(
            this.apiError(err, 'Impossible de charger les données de référence (années, niveaux).'),
          ),
      });
  }

  private reloadActiveTab(): void {
    const tab = this.activeTab();
    if (tab === 'trimestres') {
      this.reloadTrimestres();
      return;
    }
    if (tab === 'types') {
      this.reloadTypes();
      return;
    }
    this.reloadTrimestresQuota();
  }

  private resetTrimestreForm(): void {
    this.editingTrimestreId = null;
    this.trimNom = '';
    this.trimDebut = '';
    this.trimFin = '';
    this.trimNumero = '1';
    this.trimActif = true;
    const active = this.annees().find((a) => a.active);
    if (active) {
      this.trimAnneeId = String(active.id);
    }
  }

  private resetTypeForm(): void {
    this.editingTypeId = null;
    this.typeCode = '';
    this.typeLibelle = '';
    this.typeNoteMax = '20';
    this.typeOrdre = '0';
    this.typeActif = true;
    if (this.niveaux().length > 0) {
      this.typeNiveauId = String(this.niveaux()[0].id);
    }
  }

  private fetch<T>(request: Observable<T>, onSuccess: (data: T) => void, fallbackError: string): void {
    this.loading.set(true);
    request.pipe(finalize(() => this.loading.set(false))).subscribe({
      next: onSuccess,
      error: (err) => this.errorMessage.set(this.apiError(err, fallbackError)),
    });
  }

  private apiError(err: unknown, fallback: string): string {
    if (err instanceof HttpErrorResponse) {
      const body = err.error as { message?: string } | null;
      if (body?.message) return body.message;
      if (err.status === 0) {
        return 'Serveur inaccessible. Vérifiez que le backend tourne sur le port 8081.';
      }
    }
    return fallback;
  }
}
