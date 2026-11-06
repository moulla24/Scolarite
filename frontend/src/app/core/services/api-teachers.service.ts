import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EnseignantDto } from '../api/api-dto';
import { mapTeacher, toSubjectFilters } from '../api/api-mappers';
import { ReferentielApiService } from '../api/referentiel-api.service';
import { FilterOption } from '../models/filter-option.model';
import { TeachersListQuery } from '../models/list-query.model';
import { Teacher } from '../models/teacher.model';
import { TeachersDataProvider } from './interfaces/teachers-data-provider.interface';

@Injectable({ providedIn: 'root' })
export class ApiTeachersService implements TeachersDataProvider {
  private readonly http = inject(HttpClient);
  private readonly referentiel = inject(ReferentielApiService);
  private readonly base = environment.apiUrl;

  getTeachers(query?: TeachersListQuery): Observable<readonly Teacher[]> {
    const params: Record<string, string> = {};
    if (query?.search?.trim()) params['search'] = query.search.trim();
    return this.http
      .get<EnseignantDto[]>(`${this.base}/enseignants`, { params })
      .pipe(map((list) => list.map((dto) => mapTeacher(dto))));
  }

  getSubjectFilters(): Observable<readonly FilterOption[]> {
    return this.referentiel.getMatieres().pipe(map((m) => toSubjectFilters(m)));
  }
}
