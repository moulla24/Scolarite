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
import { MaquetteGrilleDto, MaquetteMatiereCardDto } from '../../../core/api/api-dto';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { toClassFilters, toLevelFilters } from '../../../core/api/api-mappers';
import { FilterOption } from '../../../core/models/filter-option.model';
import { MaquettesApiService } from './maquettes-api.service';

export interface MaquettesViewState {
  readonly levelFilters: readonly FilterOption[];
  readonly classFilters: readonly FilterOption[];
  /** Matières déjà présentes dans la maquette de la classe */
  readonly subjectFilters: readonly FilterOption[];
  readonly selectedLevelId: string;
  readonly selectedClassId: string;
  readonly selectedSubjectId: string;
  readonly needsClassSelection: boolean;
  readonly grille: MaquetteGrilleDto | null;
  /** Liste complète des matières (non filtrée par le sélecteur matière) */
  readonly allMatieres: readonly MaquetteMatiereCardDto[];
  readonly loadError: boolean;
  readonly resultCount: number;
}

@Injectable()
export class MaquettesFacadeService {
  private readonly api = inject(MaquettesApiService);
  private readonly referentiel = inject(ReferentielApiService);

  private readonly selectedLevel$ = new BehaviorSubject('all');
  private readonly selectedClass$ = new BehaviorSubject('all');
  private readonly selectedSubject$ = new BehaviorSubject('all');
  private readonly refresh$ = new BehaviorSubject(0);

  private readonly levelFilters$ = this.referentiel.getNiveaux().pipe(
    map((n) => toLevelFilters(n)),
    shareReplay(1)
  );

  readonly viewState$: Observable<MaquettesViewState> = combineLatest([
    this.selectedLevel$,
    this.selectedClass$,
    this.selectedSubject$,
    this.refresh$,
  ]).pipe(
    switchMap(([levelId, classId, subjectId]) => {
      const niveauParam = levelId === 'all' ? undefined : levelId;
      return combineLatest([
        this.levelFilters$,
        this.referentiel.getClasses(niveauParam),
      ]).pipe(
        switchMap(([levelFilters, classes]) => {
          const classFilters = toClassFilters(classes);
          const effectiveClassId =
            classId !== 'all' && classFilters.some((f) => f.id === classId) ? classId : 'all';

          if (effectiveClassId === 'all') {
            return of<MaquettesViewState>({
              levelFilters,
              classFilters,
              subjectFilters: [],
              selectedLevelId: levelId,
              selectedClassId: effectiveClassId,
              selectedSubjectId: subjectId,
              needsClassSelection: true,
              grille: null,
              allMatieres: [],
              loadError: false,
              resultCount: 0,
            });
          }

          return this.api.getGrille(effectiveClassId).pipe(
            map((grille) => {
              const subjectFilters: FilterOption[] = grille.matieres.map((m) => ({
                id: String(m.matiereId),
                label: m.matiere,
              }));
              const effectiveSubjectId =
                subjectId !== 'all' && subjectFilters.some((f) => f.id === subjectId)
                  ? subjectId
                  : 'all';
              const filtered =
                effectiveSubjectId !== 'all'
                  ? {
                      ...grille,
                      matieres: grille.matieres.filter(
                        (m) => String(m.matiereId) === effectiveSubjectId
                      ),
                    }
                  : grille;
              return {
                levelFilters,
                classFilters,
                subjectFilters,
                selectedLevelId: levelId,
                selectedClassId: effectiveClassId,
                selectedSubjectId: effectiveSubjectId,
                needsClassSelection: false,
                grille: filtered,
                allMatieres: grille.matieres,
                loadError: false,
                resultCount: filtered.matieres.length,
              };
            }),
            catchError(() =>
              of({
                levelFilters,
                classFilters,
                subjectFilters: [],
                selectedLevelId: levelId,
                selectedClassId: effectiveClassId,
                selectedSubjectId: subjectId,
                needsClassSelection: false,
                grille: null,
                allMatieres: [],
                loadError: true,
                resultCount: 0,
              })
            ),
            tap((state) => {
              if (this.selectedClass$.value !== state.selectedClassId) {
                this.selectedClass$.next(state.selectedClassId);
              }
            })
          );
        })
      );
    })
  );

  setLevel(levelId: string): void {
    this.selectedLevel$.next(levelId);
    this.selectedClass$.next('all');
    this.selectedSubject$.next('all');
  }

  setClass(classId: string): void {
    this.selectedClass$.next(classId);
    this.selectedSubject$.next('all');
  }

  setSubject(subjectId: string): void {
    this.selectedSubject$.next(subjectId);
  }

  refresh(): void {
    this.refresh$.next(this.refresh$.value + 1);
  }
}
