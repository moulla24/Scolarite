import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BehaviorSubject, combineLatest, map, Observable, shareReplay, switchMap, tap } from 'rxjs';
import { ReferentielApiService } from '../../../../core/api/referentiel-api.service';
import { toClassFilters, toLevelFilters, toSubjectFilters } from '../../../../core/api/api-mappers';
import { FilterOption } from '../../../../core/models/filter-option.model';
import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { GradeBulletinsTabComponent } from '../../components/grade-bulletins-tab/grade-bulletins-tab.component';
import { GradeSaisieTabComponent } from '../../components/grade-saisie-tab/grade-saisie-tab.component';

export type GradesTab = 'saisie' | 'bulletins';

interface GradesPageState {
  readonly levelFilters: readonly FilterOption[];
  readonly classFilters: readonly FilterOption[];
  readonly subjectFilters: readonly FilterOption[];
  readonly selectedLevelId: string;
  readonly selectedClassId: string;
  readonly selectedSubjectId: string;
}

@Component({
  selector: 'app-grades-page',
  standalone: true,
  imports: [
    AsyncPipe,
    FormsModule,
    PageHeaderComponent,
    FilterCardComponent,
    GradeSaisieTabComponent,
    GradeBulletinsTabComponent,
  ],
  templateUrl: './grades-page.component.html',
  styleUrl: './grades-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GradesPageComponent {
  private readonly referentiel = inject(ReferentielApiService);

  readonly activeTab = signal<GradesTab>('saisie');
  readonly successMessage = signal<string | null>(null);

  private readonly selectedLevel$ = new BehaviorSubject('all');
  private readonly selectedClass$ = new BehaviorSubject('all');
  private readonly selectedSubject$ = new BehaviorSubject('all');

  private readonly levelFilters$ = this.referentiel.getNiveaux().pipe(
    map((n) => toLevelFilters(n)),
    shareReplay(1)
  );

  readonly pageState$: Observable<GradesPageState> = combineLatest([
    this.selectedLevel$,
    this.selectedClass$,
    this.selectedSubject$,
  ]).pipe(
    switchMap(([levelId, classId, subjectId]) => {
      const niveauParam = levelId === 'all' ? undefined : levelId;
      return combineLatest([
        this.levelFilters$,
        this.referentiel.getClasses(niveauParam),
        this.referentiel.getMatieres(),
      ]).pipe(
        map(([levelFilters, classes, matieres]) => {
          const classFilters = toClassFilters(classes);
          const subjectFilters = toSubjectFilters(matieres).filter((f) => f.id !== 'all');
          const effectiveClassId =
            classId !== 'all' && classFilters.some((f) => f.id === classId) ? classId : 'all';
          const effectiveSubjectId =
            subjectId !== 'all' && subjectFilters.some((f) => f.id === subjectId)
              ? subjectId
              : effectiveClassId !== 'all'
                ? (subjectFilters[0]?.id ?? 'all')
                : 'all';

          return {
            levelFilters,
            classFilters,
            subjectFilters,
            selectedLevelId: levelId,
            selectedClassId: effectiveClassId,
            selectedSubjectId: effectiveSubjectId,
            syncClassId: effectiveClassId,
            syncSubjectId: effectiveSubjectId,
          };
        }),
        tap((state) => {
          if (this.selectedClass$.value !== state.syncClassId) {
            this.selectedClass$.next(state.syncClassId);
          }
          if (this.activeTab() === 'saisie' && this.selectedSubject$.value !== state.syncSubjectId) {
            this.selectedSubject$.next(state.syncSubjectId);
          }
        }),
        map(({ syncClassId: _c, syncSubjectId: _s, ...state }) => state)
      );
    })
  );

  setTab(tab: GradesTab): void {
    this.activeTab.set(tab);
    this.successMessage.set(null);
  }

  onLevelChange(levelId: string): void {
    this.selectedLevel$.next(levelId);
    this.selectedClass$.next('all');
    this.selectedSubject$.next('all');
  }

  onClassChange(classId: string): void {
    this.selectedClass$.next(classId);
    this.selectedSubject$.next('all');
  }

  onSubjectChange(subjectId: string): void {
    this.selectedSubject$.next(subjectId);
  }

  onSaisieSaved(): void {
    this.successMessage.set('Notes enregistrées pour le trimestre courant.');
  }
}
