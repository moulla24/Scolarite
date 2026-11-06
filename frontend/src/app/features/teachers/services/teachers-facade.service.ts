import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable, switchMap } from 'rxjs';
import { TEACHERS_DATA_PROVIDER } from '../../../core/constants/app.constants';
import { FilterOption } from '../../../core/models/filter-option.model';
import { Teacher } from '../../../core/models/teacher.model';
import { TeachersDataProvider } from '../../../core/services/interfaces/teachers-data-provider.interface';
import { matchesSearch } from '../../../shared/utils/list-filter.util';

export interface TeachersViewState {
  readonly teachers: readonly Teacher[];
  readonly subjectFilters: readonly FilterOption[];
  readonly searchQuery: string;
  readonly selectedSubjectId: string;
  readonly resultCount: number;
}

@Injectable()
export class TeachersFacadeService {
  private readonly dataProvider = inject<TeachersDataProvider>(TEACHERS_DATA_PROVIDER);
  private readonly searchQuery$ = new BehaviorSubject('');
  private readonly selectedSubject$ = new BehaviorSubject('all');
  private readonly refresh$ = new BehaviorSubject(0);
  private readonly createFormOpenSubject$ = new BehaviorSubject(false);
  private readonly editIdSubject$ = new BehaviorSubject<number | null>(null);

  readonly createFormOpen$ = this.createFormOpenSubject$.asObservable();
  readonly editId$ = this.editIdSubject$.asObservable();

  readonly viewState$: Observable<TeachersViewState> = combineLatest([
    this.searchQuery$,
    this.selectedSubject$,
    this.refresh$,
  ]).pipe(
    switchMap(([search, subjectId, _refresh]) =>
      combineLatest([
        this.dataProvider.getSubjectFilters(),
        this.dataProvider.getTeachers({ search: search.trim() || undefined }),
      ]).pipe(
        map(([subjectFilters, teachers]) => {
          const subjectLabel =
            subjectId === 'all'
              ? ''
              : (subjectFilters.find((f) => f.id === subjectId)?.label?.toLowerCase() ?? '');
          const filtered = teachers.filter(
            (t) =>
              matchesSearch(search, t.fullName, t.email, t.phone) &&
              (subjectId === 'all' ||
                t.subjects.some((s) => s.toLowerCase().includes(subjectLabel)))
          );
          return {
            teachers: filtered,
            subjectFilters,
            searchQuery: search,
            selectedSubjectId: subjectId,
            resultCount: filtered.length,
          };
        })
      )
    )
  );

  setSearch(query: string): void {
    this.searchQuery$.next(query);
  }

  setSubject(subjectId: string): void {
    this.selectedSubject$.next(subjectId);
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
