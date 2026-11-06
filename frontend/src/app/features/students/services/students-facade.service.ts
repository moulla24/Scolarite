import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable, switchMap } from 'rxjs';
import { STUDENTS_DATA_PROVIDER } from '../../../core/constants/app.constants';
import { FilterOption } from '../../../core/models/filter-option.model';
import { Student } from '../../../core/models/student.model';
import { StudentsDataProvider } from '../../../core/services/interfaces/students-data-provider.interface';
import { matchesSearch } from '../../../shared/utils/list-filter.util';

export interface StudentsViewState {
  readonly students: readonly Student[];
  readonly levelFilters: readonly FilterOption[];
  readonly classFilters: readonly FilterOption[];
  readonly searchQuery: string;
  readonly selectedLevelId: string;
  readonly selectedClassId: string;
  readonly resultCount: number;
}

@Injectable()
export class StudentsFacadeService {
  private readonly dataProvider = inject<StudentsDataProvider>(STUDENTS_DATA_PROVIDER);
  private readonly searchQuery$ = new BehaviorSubject('');
  private readonly selectedLevel$ = new BehaviorSubject('all');
  private readonly selectedClass$ = new BehaviorSubject('all');
  private readonly refresh$ = new BehaviorSubject(0);
  private readonly createFormOpenSubject$ = new BehaviorSubject(false);
  private readonly editIdSubject$ = new BehaviorSubject<number | null>(null);

  readonly createFormOpen$ = this.createFormOpenSubject$.asObservable();
  readonly editId$ = this.editIdSubject$.asObservable();

  readonly viewState$: Observable<StudentsViewState> = combineLatest([
    this.searchQuery$,
    this.selectedLevel$,
    this.selectedClass$,
    this.refresh$,
  ]).pipe(
    switchMap(([search, levelId, classId, _refresh]) => {
      const niveauParam = levelId === 'all' ? undefined : levelId;
      return combineLatest([
        this.dataProvider.getLevelFilters(),
        this.dataProvider.getClassFilters(niveauParam),
        this.dataProvider.getStudents({
          search: search.trim() || undefined,
          niveauId: niveauParam,
          classeId: classId === 'all' ? undefined : classId,
        }),
      ]).pipe(
        map(([levelFilters, classFilters, students]) => {
          const validClassId = classFilters.some((f) => f.id === classId) ? classId : 'all';
          const filtered = students.filter((s) =>
            matchesSearch(search, s.fullName, s.matricule, s.className)
          );
          return {
            students: filtered,
            levelFilters,
            classFilters,
            searchQuery: search,
            selectedLevelId: levelId,
            selectedClassId: validClassId,
            resultCount: filtered.length,
          };
        })
      );
    })
  );

  setSearch(query: string): void {
    this.searchQuery$.next(query);
  }

  setLevel(levelId: string): void {
    this.selectedLevel$.next(levelId);
    this.selectedClass$.next('all');
  }

  setClass(classId: string): void {
    this.selectedClass$.next(classId);
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
