import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable, switchMap } from 'rxjs';
import { CLASSROOMS_DATA_PROVIDER } from '../../../core/constants/app.constants';
import { Classroom } from '../../../core/models/classroom.model';
import { FilterOption } from '../../../core/models/filter-option.model';
import { ClassroomsDataProvider } from '../../../core/services/interfaces/classrooms-data-provider.interface';
import { matchesSearch } from '../../../shared/utils/list-filter.util';

export interface ClassroomsViewState {
  readonly classrooms: readonly Classroom[];
  readonly levelFilters: readonly FilterOption[];
  readonly searchQuery: string;
  readonly selectedLevelId: string;
  readonly resultCount: number;
}

@Injectable()
export class ClassroomsFacadeService {
  private readonly dataProvider = inject<ClassroomsDataProvider>(CLASSROOMS_DATA_PROVIDER);
  private readonly searchQuery$ = new BehaviorSubject('');
  private readonly selectedLevel$ = new BehaviorSubject('all');
  private readonly refresh$ = new BehaviorSubject(0);
  private readonly createFormOpenSubject$ = new BehaviorSubject(false);
  private readonly editIdSubject$ = new BehaviorSubject<number | null>(null);

  readonly createFormOpen$ = this.createFormOpenSubject$.asObservable();
  readonly editId$ = this.editIdSubject$.asObservable();

  readonly viewState$: Observable<ClassroomsViewState> = combineLatest([
    this.searchQuery$,
    this.selectedLevel$,
    this.refresh$,
  ]).pipe(
    switchMap(([search, levelId, _refresh]) =>
      combineLatest([
        this.dataProvider.getLevelFilters(),
        this.dataProvider.getClassrooms({
          search: search.trim() || undefined,
          niveauId: levelId === 'all' ? undefined : levelId,
        }),
      ]).pipe(
        map(([levelFilters, classrooms]) => {
          const filtered = classrooms.filter((c) =>
            matchesSearch(search, c.name, c.teacherName, c.room)
          );
          return {
            classrooms: filtered,
            levelFilters,
            searchQuery: search,
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

  setLevel(levelId: string): void {
    this.selectedLevel$.next(levelId);
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

  closeCreateForm(): void {
    this.createFormOpenSubject$.next(false);
    this.editIdSubject$.next(null);
  }

  refreshList(): void {
    this.refresh$.next(this.refresh$.value + 1);
  }
}
