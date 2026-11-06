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

import { GrilleNotesDto, NotesSummaryLigneDto } from '../../../core/api/api-dto';

import { ReferentielApiService } from '../../../core/api/referentiel-api.service';

import { toClassFilters, toLevelFilters, toSubjectFilters } from '../../../core/api/api-mappers';

import { FilterOption } from '../../../core/models/filter-option.model';

import { GradesApiService } from './grades-api.service';



export interface GradesViewState {

  readonly grille: GrilleNotesDto;

  readonly summaryRows: readonly NotesSummaryLigneDto[];

  readonly summaryMode: boolean;

  readonly classFilters: readonly FilterOption[];

  readonly subjectFilters: readonly FilterOption[];

  readonly levelFilters: readonly FilterOption[];

  readonly selectedClassId: string;

  readonly selectedSubjectId: string;

  readonly selectedLevelId: string;

  readonly resultCount: number;

  readonly needsClassSelection: boolean;

  readonly loadError: boolean;

}



const emptyGrille = (): GrilleNotesDto => ({ colonnes: [], lignes: [] });

const emptySummary: NotesSummaryLigneDto[] = [];



function normalizeFilterId(id: string): string {

  return id === 'all' ? 'all' : String(id);

}



@Injectable()

export class GradesFacadeService {

  private readonly api = inject(GradesApiService);

  private readonly referentiel = inject(ReferentielApiService);



  private readonly selectedClass$ = new BehaviorSubject('all');

  private readonly selectedSubject$ = new BehaviorSubject('all');

  private readonly selectedLevel$ = new BehaviorSubject('all');

  private readonly refresh$ = new BehaviorSubject(0);



  private readonly levelFilters$ = this.referentiel.getNiveaux().pipe(

    map((n) => toLevelFilters(n)),

    shareReplay(1)

  );



  readonly viewState$: Observable<GradesViewState> = combineLatest([

    this.selectedClass$,

    this.selectedSubject$,

    this.selectedLevel$,

    this.refresh$,

  ]).pipe(

    switchMap(([classId, subjectId, levelId]) => {

      const normalizedLevelId = normalizeFilterId(levelId);

      const normalizedSubjectId = normalizeFilterId(subjectId);

      const normalizedClassId = normalizeFilterId(classId);

      const niveauParam = normalizedLevelId === 'all' ? undefined : normalizedLevelId;

      const summaryMode = normalizedSubjectId === 'all';



      return combineLatest([

        this.levelFilters$,

        this.referentiel.getClasses(niveauParam),

        this.referentiel.getMatieres(),

      ]).pipe(

        switchMap(([levelFilters, classes, matieres]) => {

          const classFilters = toClassFilters(classes);

          const effectiveClassId =

            normalizedClassId !== 'all' &&

            classFilters.some((f) => f.id === normalizedClassId)

              ? normalizedClassId

              : 'all';



          const data$ =

            effectiveClassId === 'all'

              ? of({ grille: emptyGrille(), summary: emptySummary, loadError: false })

              : summaryMode

                ? this.api.getSummary(effectiveClassId, niveauParam).pipe(

                    map((summary) => ({

                      grille: emptyGrille(),

                      summary: [...summary],

                      loadError: false,

                    })),

                    catchError(() =>

                      of({ grille: emptyGrille(), summary: emptySummary, loadError: true })

                    )

                  )

                : this.api.getGrille(effectiveClassId, niveauParam, normalizedSubjectId).pipe(

                    map((grille) => ({ grille, summary: emptySummary, loadError: false })),

                    catchError(() =>

                      of({ grille: emptyGrille(), summary: emptySummary, loadError: true })

                    )

                  );



          return data$.pipe(

            map((data) => ({

              grille: data.grille,

              summaryRows: data.summary,

              summaryMode: summaryMode && effectiveClassId !== 'all',

              classFilters,

              subjectFilters: toSubjectFilters(matieres),

              levelFilters,

              selectedClassId: effectiveClassId,

              selectedSubjectId: normalizedSubjectId,

              selectedLevelId: normalizedLevelId,

              resultCount: summaryMode ? data.summary.length : data.grille.lignes.length,

              needsClassSelection: effectiveClassId === 'all',

              loadError: data.loadError,

            }))

          );

        }),

        tap((state) => {

          if (this.selectedClass$.value !== state.selectedClassId) {

            this.selectedClass$.next(state.selectedClassId);

          }

        })

      );

    })

  );



  setClass(classId: string): void {

    this.selectedClass$.next(normalizeFilterId(classId));

  }



  setSubject(subjectId: string): void {

    this.selectedSubject$.next(normalizeFilterId(subjectId));

  }



  setLevel(levelId: string): void {

    this.selectedLevel$.next(normalizeFilterId(levelId));

    this.selectedClass$.next('all');

    this.selectedSubject$.next('all');

  }



  refresh(): void {

    this.refresh$.next(this.refresh$.value + 1);

  }

}


