import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable, switchMap } from 'rxjs';
import { PARENTS_DATA_PROVIDER } from '../../../core/constants/app.constants';
import { FilterOption } from '../../../core/models/filter-option.model';
import { Parent } from '../../../core/models/parent.model';
import { ParentsDataProvider } from '../../../core/services/interfaces/parents-data-provider.interface';
import { matchesSearch } from '../../../shared/utils/list-filter.util';

export const PARENT_STATUT_FILTERS: readonly FilterOption[] = [
  { id: 'all', label: 'Tous les statuts' },
  { id: 'A_JOUR', label: 'À jour' },
  { id: 'EN_RETARD', label: 'Dette' },
];

export interface ParentsViewState {
  readonly parents: readonly Parent[];
  readonly levelFilters: readonly FilterOption[];
  readonly statutFilters: readonly FilterOption[];
  readonly searchQuery: string;
  readonly selectedLevelId: string;
  readonly selectedStatutId: string;
  readonly resultCount: number;
}

@Injectable()
export class ParentsFacadeService {
  private readonly dataProvider = inject<ParentsDataProvider>(PARENTS_DATA_PROVIDER);
  private readonly searchQuery$ = new BehaviorSubject('');
  private readonly selectedLevel$ = new BehaviorSubject('all');
  private readonly selectedStatut$ = new BehaviorSubject('all');
  private readonly refresh$ = new BehaviorSubject(0);
  private readonly createFormOpenSubject$ = new BehaviorSubject(false);
  private readonly editIdSubject$ = new BehaviorSubject<number | null>(null);

  readonly createFormOpen$ = this.createFormOpenSubject$.asObservable();
  readonly editId$ = this.editIdSubject$.asObservable();

  readonly viewState$: Observable<ParentsViewState> = combineLatest([
    this.searchQuery$,
    this.selectedLevel$,
    this.selectedStatut$,
    this.refresh$,
  ]).pipe(
    switchMap(([search, levelId, statutId, _refresh]) =>
      combineLatest([
        this.dataProvider.getSchoolLevelFilters(),
        this.dataProvider.getParents({
          search: search.trim() || undefined,
          niveauId: levelId === 'all' ? undefined : levelId,
          statutCompte: statutId === 'all' ? undefined : statutId,
        }),
      ]).pipe(
        map(([levelFilters, parents]) => {
          const filtered = parents.filter((p) =>
            matchesSearch(search, p.fullName, p.email, p.phone)
          );
          return {
            parents: filtered,
            levelFilters,
            statutFilters: PARENT_STATUT_FILTERS,
            searchQuery: search,
            selectedLevelId: levelId,
            selectedStatutId: statutId,
            resultCount: filtered.length,
          };
        })
      )
    )
  );

  setSearch(query: string): void {
    this.searchQuery$.next(query);
  }

  setLevel(levelId: string): void {
    this.selectedLevel$.next(levelId);
  }

  setStatut(statutId: string): void {
    this.selectedStatut$.next(statutId);
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
