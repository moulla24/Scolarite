import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable, of, shareReplay, switchMap } from 'rxjs';
import { TrimestreDto } from '../../../core/api/api-dto';
import { toClassFilters, toLevelFilters } from '../../../core/api/api-mappers';
import { ReferentielApiService } from '../../../core/api/referentiel-api.service';
import { SCHEDULE_DAYS } from '../../../core/api/api-mappers';
import { SCHEDULE_DATA_PROVIDER } from '../../../core/constants/app.constants';
import { FilterOption } from '../../../core/models/filter-option.model';
import { ScheduleDay, ScheduleSlot } from '../../../core/models/schedule.model';
import { ScheduleDataProvider } from '../../../core/services/interfaces/schedule-data-provider.interface';
import { PlanificationApiService } from '../../config-plan/services/planification-api.service';
import {
  buildDaysForWeek,
  findCurrentTrimester,
  findCurrentWeekIndex,
  isCurrentWeek,
  mapSemaineDto,
  resolveTimeSlotStart,
  SCHEDULE_TIME_SLOTS,
  ScheduleWeek,
} from '../utils/schedule-week.util';

export interface ScheduleViewState {
  readonly slots: readonly ScheduleSlot[];
  readonly days: readonly ScheduleDay[];
  readonly levelFilters: readonly FilterOption[];
  readonly classFilters: readonly FilterOption[];
  readonly selectedLevelId: string;
  readonly selectedClassId: string;
  readonly filtersReady: boolean;
  readonly timeRows: readonly string[];
  readonly grid: ReadonlyMap<string, ScheduleSlot | undefined>;
  readonly trimestre: TrimestreDto | null;
  readonly weeks: readonly ScheduleWeek[];
  readonly selectedWeekIndex: number;
  readonly selectedWeek: ScheduleWeek | null;
  readonly isCurrentWeekSelected: boolean;
}

const AUTO_WEEK = -1;

@Injectable()
export class ScheduleFacadeService {
  private readonly dataProvider = inject<ScheduleDataProvider>(SCHEDULE_DATA_PROVIDER);
  private readonly planification = inject(PlanificationApiService);
  private readonly referentiel = inject(ReferentielApiService);
  private readonly selectedLevel$ = new BehaviorSubject('all');
  private readonly selectedClass$ = new BehaviorSubject('all');
  private readonly selectedWeekIndex$ = new BehaviorSubject(AUTO_WEEK);
  private readonly refresh$ = new BehaviorSubject(0);

  private readonly levelFilters$ = this.referentiel.getNiveaux().pipe(
    map((n) => toLevelFilters(n)),
    shareReplay(1)
  );

  readonly viewState$: Observable<ScheduleViewState> = combineLatest([
    this.selectedLevel$,
    this.selectedClass$,
    this.selectedWeekIndex$,
    this.planification.listTrimestres(),
    this.levelFilters$,
    this.refresh$,
  ]).pipe(
    switchMap(([levelId, classId, weekIndex, trimestres, levelFilters]) => {
      const trimestre = findCurrentTrimester(trimestres);
      if (!trimestre) {
        return this.referentiel.getClasses().pipe(
          map((classes) => this.emptyState(levelId, classId, levelFilters, toClassFilters(classes)))
        );
      }

      return this.referentiel.getClasses(levelId === 'all' ? undefined : levelId).pipe(
        switchMap((classes) => {
          const classFilters = toClassFilters(classes);
          const filtersReady = levelId !== 'all' && classId !== 'all';

          return this.planification.listSemaines(trimestre.id).pipe(
            switchMap((semaineDtos) => {
              const weeks = semaineDtos.map(mapSemaineDto);
              const resolvedWeekIndex =
                weekIndex >= 0 && weekIndex < weeks.length ? weekIndex : findCurrentWeekIndex(weeks);
              const selectedWeek = weeks[resolvedWeekIndex] ?? null;
              const days = selectedWeek ? buildDaysForWeek(selectedWeek) : [...SCHEDULE_DAYS];
              const semaineId = selectedWeek ? String(selectedWeek.id) : undefined;
              const timeRows = SCHEDULE_TIME_SLOTS.map((s) => s.label);

              if (!filtersReady) {
                return of(
                  this.buildViewState({
                    slots: [],
                    days,
                    levelFilters,
                    classFilters,
                    levelId,
                    classId,
                    filtersReady,
                    timeRows,
                    grid: new Map(),
                    trimestre,
                    weeks,
                    resolvedWeekIndex,
                    selectedWeek,
                  })
                );
              }

              return this.dataProvider
                .getSlots({ classeId: classId, semaineId })
                .pipe(
                  map((slots) => {
                    const grid = new Map<string, ScheduleSlot | undefined>();
                    for (const slot of slots) {
                      const rowStart = resolveTimeSlotStart(slot.startTime);
                      if (!rowStart) continue;
                      const key = `${slot.dayIndex}|${rowStart}`;
                      if (!grid.has(key)) {
                        grid.set(key, slot);
                      }
                    }
                    return this.buildViewState({
                      slots,
                      days,
                      levelFilters,
                      classFilters,
                      levelId,
                      classId,
                      filtersReady,
                      timeRows,
                      grid,
                      trimestre,
                      weeks,
                      resolvedWeekIndex,
                      selectedWeek,
                    });
                  })
                );
            })
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

  setWeek(index: number): void {
    this.selectedWeekIndex$.next(index);
  }

  goToCurrentWeek(): void {
    this.selectedWeekIndex$.next(AUTO_WEEK);
  }

  getSlot(
    grid: ReadonlyMap<string, ScheduleSlot | undefined>,
    dayIndex: number,
    timeLabel: string
  ): ScheduleSlot | undefined {
    const startTime = timeLabel.split(' - ')[0];
    return grid.get(`${dayIndex}|${startTime}`);
  }

  refresh(): void {
    this.refresh$.next(this.refresh$.value + 1);
  }

  private emptyState(
    levelId: string,
    classId: string,
    levelFilters: readonly FilterOption[],
    classFilters: readonly FilterOption[]
  ): ScheduleViewState {
    return this.buildViewState({
      slots: [],
      days: [...SCHEDULE_DAYS],
      levelFilters,
      classFilters,
      levelId,
      classId,
      filtersReady: levelId !== 'all' && classId !== 'all',
      timeRows: SCHEDULE_TIME_SLOTS.map((s) => s.label),
      grid: new Map(),
      trimestre: null,
      weeks: [],
      resolvedWeekIndex: 0,
      selectedWeek: null,
    });
  }

  private buildViewState(params: {
    slots: readonly ScheduleSlot[];
    days: readonly ScheduleDay[];
    levelFilters: readonly FilterOption[];
    classFilters: readonly FilterOption[];
    levelId: string;
    classId: string;
    filtersReady: boolean;
    timeRows: readonly string[];
    grid: ReadonlyMap<string, ScheduleSlot | undefined>;
    trimestre: TrimestreDto | null;
    weeks: readonly ScheduleWeek[];
    resolvedWeekIndex: number;
    selectedWeek: ScheduleWeek | null;
  }): ScheduleViewState {
    return {
      slots: params.slots,
      days: params.days,
      levelFilters: params.levelFilters,
      classFilters: params.classFilters,
      selectedLevelId: params.levelId,
      selectedClassId: params.classId,
      filtersReady: params.filtersReady,
      timeRows: params.timeRows,
      grid: params.grid,
      trimestre: params.trimestre,
      weeks: params.weeks,
      selectedWeekIndex: params.resolvedWeekIndex,
      selectedWeek: params.selectedWeek,
      isCurrentWeekSelected: params.selectedWeek ? isCurrentWeek(params.selectedWeek) : false,
    };
  }
}
