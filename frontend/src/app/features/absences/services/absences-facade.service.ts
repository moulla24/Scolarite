import { inject, Injectable } from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  map,
  Observable,
  of,
  shareReplay,
  switchMap,
  tap,
} from 'rxjs';
import { FeuillePresenceDto } from '../../../core/api/api-dto';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { toClassFilters, toLevelFilters } from '../../../core/api/api-mappers';
import { FilterOption } from '../../../core/models/filter-option.model';
import { AbsencesApiService } from './absences-api.service';

export interface AbsencesViewState {
  readonly levelFilters: readonly FilterOption[];
  readonly classFilters: readonly FilterOption[];
  readonly selectedLevelId: string;
  readonly selectedClassId: string;
  readonly selectedDate: string;
  readonly needsClassSelection: boolean;
  readonly feuille: FeuillePresenceDto | null;
  readonly loadError: boolean;
  readonly loading: boolean;
}

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

function normalizeFilterId(id: string): string {
  return id === 'all' ? 'all' : String(id);
}

@Injectable()
export class AbsencesFacadeService {
  private readonly api = inject(AbsencesApiService);
  private readonly referentiel = inject(ReferentielApiService);

  private readonly selectedLevel$ = new BehaviorSubject('all');
  private readonly selectedClass$ = new BehaviorSubject('all');
  private readonly selectedDate$ = new BehaviorSubject(todayIso());
  private readonly refresh$ = new BehaviorSubject(0);

  private readonly levelFilters$ = this.referentiel.getNiveaux().pipe(
    map((n) => toLevelFilters(n)),
    shareReplay(1)
  );

  readonly viewState$: Observable<AbsencesViewState> = combineLatest([
    this.selectedLevel$,
    this.selectedClass$,
    this.selectedDate$,
    this.refresh$,
  ]).pipe(
    switchMap(([levelId, classId, date]) => {
      const niveauParam = levelId === 'all' ? undefined : levelId;
      const needsClass = classId === 'all';

      return combineLatest([
        this.levelFilters$,
        this.referentiel.getClasses(niveauParam),
      ]).pipe(
        switchMap(([levelFilters, classes]) => {
          const classFilters = toClassFilters(classes);
          let effectiveClassId = normalizeFilterId(classId);

          if (
            effectiveClassId !== 'all' &&
            !classFilters.some((c) => c.id === effectiveClassId)
          ) {
            effectiveClassId = 'all';
            this.selectedClass$.next('all');
          }

          if (needsClass || effectiveClassId === 'all') {
            return of<AbsencesViewState>({
              levelFilters,
              classFilters,
              selectedLevelId: levelId,
              selectedClassId: effectiveClassId,
              selectedDate: date,
              needsClassSelection: true,
              feuille: null,
              loadError: false,
              loading: false,
            });
          }

          return this.api.getFeuille(effectiveClassId, date).pipe(
            map((feuille) => ({
              levelFilters,
              classFilters,
              selectedLevelId: levelId,
              selectedClassId: effectiveClassId,
              selectedDate: date,
              needsClassSelection: false,
              feuille,
              loadError: false,
              loading: false,
            })),
            catchError(() =>
              of({
                levelFilters,
                classFilters,
                selectedLevelId: levelId,
                selectedClassId: effectiveClassId,
                selectedDate: date,
                needsClassSelection: false,
                feuille: null,
                loadError: true,
                loading: false,
              })
            )
          );
        })
      );
    })
  );

  setLevel(levelId: string): void {
    this.selectedLevel$.next(levelId);
    this.selectedClass$.next('all');
  }

  setClass(classId: string): void {
    this.selectedClass$.next(classId);
  }

  setDate(date: string): void {
    this.selectedDate$.next(date);
  }

  refresh(): void {
    this.refresh$.next(this.refresh$.value + 1);
  }
}
