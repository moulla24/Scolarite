import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import {
  FeuillePresenceDto,
  FeuillePresenceSaveRequest,
  PresenceStatut,
} from '../../../../core/api/api-dto';
import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { AbsencesGridComponent } from '../../components/absences-grid/absences-grid.component';
import { AbsencesApiService } from '../../services/absences-api.service';
import { AbsencesFacadeService } from '../../services/absences-facade.service';

@Component({
  selector: 'app-absences-page',
  standalone: true,
  imports: [
    AsyncPipe,
    FormsModule,
    PageHeaderComponent,
    FilterCardComponent,
    AbsencesGridComponent,
  ],
  providers: [AbsencesFacadeService],
  templateUrl: './absences-page.component.html',
  styleUrl: './absences-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AbsencesPageComponent {
  private readonly facade = inject(AbsencesFacadeService);
  private readonly api = inject(AbsencesApiService);
  private readonly destroyRef = inject(DestroyRef);

  readonly viewState$ = this.facade.viewState$;
  readonly editableFeuille = signal<FeuillePresenceDto | null>(null);
  readonly saving = signal(false);
  readonly dirty = signal(false);
  readonly successMessage = signal<string | null>(null);

  private lastFeuilleKey = '';

  constructor() {
    this.facade.viewState$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((view) => {
        const key = view.feuille
          ? `${view.selectedClassId}-${view.selectedDate}`
          : '';
        if (view.feuille && key !== this.lastFeuilleKey) {
          this.lastFeuilleKey = key;
          this.editableFeuille.set(structuredClone(view.feuille));
          this.dirty.set(false);
        }
        if (!view.feuille) {
          this.lastFeuilleKey = '';
          this.editableFeuille.set(null);
          this.dirty.set(false);
        }
      });
  }

  onLevelChange(levelId: string): void {
    this.successMessage.set(null);
    this.facade.setLevel(levelId);
  }

  onClassChange(classId: string): void {
    this.successMessage.set(null);
    this.facade.setClass(classId);
  }

  onDateChange(date: string): void {
    this.successMessage.set(null);
    this.facade.setDate(date);
  }

  onStatutChange(event: { plage: string; eleveId: number; statut: PresenceStatut }): void {
    const feuille = this.editableFeuille();
    if (!feuille) return;

    this.editableFeuille.set({
      ...feuille,
      plages: feuille.plages.map((plage) => {
        if (plage.plage !== event.plage) return plage;
        return {
          ...plage,
          eleves: plage.eleves.map((eleve) =>
            eleve.eleveId === event.eleveId ? { ...eleve, statut: event.statut } : eleve
          ),
        };
      }),
    });
    this.dirty.set(true);
  }

  onSave(): void {
    const feuille = this.editableFeuille();
    if (!feuille || this.saving()) return;

    const request: FeuillePresenceSaveRequest = {
      date: feuille.date,
      classeId: feuille.classeId,
      lignes: feuille.plages.flatMap((plage) =>
        plage.eleves.map((eleve) => ({
          plage: plage.plage,
          eleveId: eleve.eleveId,
          statut: eleve.statut,
          motif: eleve.motif,
        }))
      ),
    };

    this.saving.set(true);
    this.api.saveFeuille(request).subscribe({
      next: (saved) => {
        this.editableFeuille.set(structuredClone(saved));
        this.dirty.set(false);
        this.saving.set(false);
        this.lastFeuilleKey = `${feuille.classeId}-${feuille.date}`;
        this.successMessage.set('Présences enregistrées avec succès.');
      },
      error: () => {
        this.saving.set(false);
        alert('Enregistrement impossible. Réessayez.');
      },
    });
  }

  studentCount(feuille: FeuillePresenceDto | null): number {
    return feuille?.plages[0]?.eleves.length ?? 0;
  }
}
