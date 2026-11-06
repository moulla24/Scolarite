import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable, switchMap } from 'rxjs';
import { matchesSearch } from '../../../shared/utils/list-filter.util';
import { Comptable, ComptablesApiService } from './comptables-api.service';

export interface ComptablesViewState {
  readonly comptables: readonly Comptable[];
  readonly searchQuery: string;
  readonly resultCount: number;
}

@Injectable()
export class ComptablesFacadeService {
  private readonly api = inject(ComptablesApiService);
  private readonly searchQuery$ = new BehaviorSubject('');
  private readonly refresh$ = new BehaviorSubject(0);
  private readonly createFormOpenSubject$ = new BehaviorSubject(false);
  private readonly editIdSubject$ = new BehaviorSubject<number | null>(null);

  readonly createFormOpen$ = this.createFormOpenSubject$.asObservable();
  readonly editId$ = this.editIdSubject$.asObservable();

  readonly viewState$: Observable<ComptablesViewState> = combineLatest([
    this.searchQuery$,
    this.refresh$,
  ]).pipe(
    switchMap(([search]) =>
      this.api.list(search.trim() || undefined).pipe(
        map((comptables) => {
          const filtered = comptables.filter((c) =>
            matchesSearch(search, c.fullName, c.email, c.phone, c.matricule)
          );
          return {
            comptables: filtered,
            searchQuery: search,
            resultCount: filtered.length,
          };
        })
      )
    )
  );

  setSearch(query: string): void {
    this.searchQuery$.next(query);
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
