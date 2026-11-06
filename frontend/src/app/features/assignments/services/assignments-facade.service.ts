import { inject, Injectable } from '@angular/core';
import {
  BehaviorSubject,
  combineLatest,
  map,
  Observable,
  shareReplay,
  switchMap,
} from 'rxjs';
import { EnseignementDto } from '../../../core/api/api-dto';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { toClassFilters, toLevelFilters, toSubjectFilters } from '../../../core/api/api-mappers';
import { FilterOption } from '../../../core/models/filter-option.model';
import { matchesSearch } from '../../../shared/utils/list-filter.util';
import { AssignmentsApiService } from './assignments-api.service';

export interface AssignmentsViewState {
  readonly assignments: readonly EnseignementDto[];
  readonly classFilters: readonly FilterOption[];
  readonly subjectFilters: readonly FilterOption[];
  readonly levelFilters: readonly FilterOption[];
  readonly searchQuery: string;
  readonly selectedClassId: string;
  readonly selectedSubjectId: string;
  readonly selectedLevelId: string;
  readonly resultCount: number;
}

@Injectable()
export class AssignmentsFacadeService {
  private readonly api = inject(AssignmentsApiService);
  private readonly referentiel = inject(ReferentielApiService);

  private readonly searchQuery$ = new BehaviorSubject('');
  private readonly selectedClass$ = new BehaviorSubject('all');
  private readonly selectedSubject$ = new BehaviorSubject('all');
  private readonly selectedLevel$ = new BehaviorSubject('all');
  private readonly refresh$ = new BehaviorSubject(0);
  private readonly createFormOpenSubject$ = new BehaviorSubject(false);
  private readonly editIdSubject$ = new BehaviorSubject<number | null>(null);

  private readonly levelFilters$ = this.referentiel.getNiveaux().pipe(
    map((n) => toLevelFilters(n)),
    shareReplay(1)
  );

  readonly createFormOpen$ = this.createFormOpenSubject$.asObservable();
  readonly editId$ = this.editIdSubject$.asObservable();

  readonly viewState$: Observable<AssignmentsViewState> = combineLatest([
    this.searchQuery$,
    this.selectedClass$,
    this.selectedSubject$,
    this.selectedLevel$,
    this.refresh$,
  ]).pipe(
    switchMap(([search, classId, subjectId, levelId]) =>
      combineLatest([
        this.levelFilters$,
        this.referentiel.getClasses(levelId === 'all' ? undefined : levelId, search.trim() || undefined),
        this.referentiel.getMatieres(),
        this.api.list(
          classId === 'all' ? undefined : classId,
          subjectId === 'all' ? undefined : subjectId
        ),
      ]).pipe(
        map(([levelFilters, classes, matieres, assignments]) => {
          const classFilters = toClassFilters(classes);
          const subjectFilters = toSubjectFilters(matieres);
          const filtered = assignments.filter(
            (a) =>
              matchesSearch(search, a.libelle, a.classe, a.matiere, a.enseignant) &&
              (levelId === 'all' ||
                classes.some((c) => c.id === a.classeId))
          );
          return {
            assignments: filtered,
            classFilters,
            subjectFilters,
            levelFilters,
            searchQuery: search,
            selectedClassId: classId,
            selectedSubjectId: subjectId,
            selectedLevelId: levelId,
            resultCount: filtered.length,
          };
        })
      )
    )
  );

  setSearch(query: string): void {
    this.searchQuery$.next(query);
  }

  setClass(classId: string): void {
    this.selectedClass$.next(classId);
  }

  setSubject(subjectId: string): void {
    this.selectedSubject$.next(subjectId);
  }

  setLevel(levelId: string): void {
    this.selectedLevel$.next(levelId);
    this.selectedClass$.next('all');
  }

  toggleCreateForm(): void {
    if (this.createFormOpenSubject$.value) {
      this.closeCreateForm();
    } else {
      this.openCreateForm();
    }
  }

  openCreateForm(): void {
    this.editIdSubject$.next(null);
    this.createFormOpenSubject$.next(true);
  }

  openEditForm(id: number): void {
    this.editIdSubject$.next(id);
    this.createFormOpenSubject$.next(true);
  }

  closeCreateForm(): void {
    this.createFormOpenSubject$.next(false);
    this.editIdSubject$.next(null);
  }

  refreshList(): void {
    this.refresh$.next(this.refresh$.value + 1);
  }
}
