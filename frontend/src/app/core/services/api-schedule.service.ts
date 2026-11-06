import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SeanceEmploiDto } from '../api/api-dto';
import { mapSeance, SCHEDULE_DAYS, toClassFilters } from '../api/api-mappers';
import { ReferentielApiService } from '../api/referentiel-api.service';
import { FilterOption } from '../models/filter-option.model';
import { ScheduleListQuery } from '../models/list-query.model';
import { ScheduleDay, ScheduleSlot } from '../models/schedule.model';
import { ScheduleDataProvider } from './interfaces/schedule-data-provider.interface';

@Injectable({ providedIn: 'root' })
export class ApiScheduleService implements ScheduleDataProvider {
  private readonly http = inject(HttpClient);
  private readonly referentiel = inject(ReferentielApiService);
  private readonly base = environment.apiUrl;

  getSlots(query?: ScheduleListQuery): Observable<readonly ScheduleSlot[]> {
    const params: Record<string, string> = {};
    if (query?.classeId && query.classeId !== 'all') params['classeId'] = query.classeId;
    if (query?.semaineId) params['semaineId'] = query.semaineId;
    return this.http
      .get<SeanceEmploiDto[]>(`${this.base}/emploi-du-temps`, { params })
      .pipe(map((list) => list.map((dto) => mapSeance(dto))));
  }

  getDays(): Observable<readonly ScheduleDay[]> {
    return of(SCHEDULE_DAYS);
  }

  getClassFilters(): Observable<readonly FilterOption[]> {
    return this.referentiel.getClasses().pipe(map((c) => toClassFilters(c)));
  }
}
