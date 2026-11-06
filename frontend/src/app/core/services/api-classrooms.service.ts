import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { combineLatest, map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ClasseDto } from '../api/api-dto';
import { mapClassroom, toLevelFilters } from '../api/api-mappers';
import { ReferentielApiService } from '../api/referentiel-api.service';
import { Classroom } from '../models/classroom.model';
import { FilterOption } from '../models/filter-option.model';
import { ClassroomsListQuery } from '../models/list-query.model';
import { ClassroomsDataProvider } from './interfaces/classrooms-data-provider.interface';

@Injectable({ providedIn: 'root' })
export class ApiClassroomsService implements ClassroomsDataProvider {
  private readonly http = inject(HttpClient);
  private readonly referentiel = inject(ReferentielApiService);
  private readonly base = environment.apiUrl;

  getClassrooms(query?: ClassroomsListQuery): Observable<readonly Classroom[]> {
    const params: Record<string, string> = {};
    if (query?.search?.trim()) params['search'] = query.search.trim();
    if (query?.niveauId && query.niveauId !== 'all') params['niveauId'] = query.niveauId;
    return combineLatest([
      this.http.get<ClasseDto[]>(`${this.base}/classes`, { params }),
      this.referentiel.getNiveaux(),
    ]).pipe(
      map(([classes, niveaux]) => {
        const niveauByNom = new Map(niveaux.map((n) => [n.nom, String(n.id)]));
        return classes.map((dto) =>
          mapClassroom(dto, niveauByNom.get(dto.niveau) ?? '')
        );
      })
    );
  }

  getLevelFilters(): Observable<readonly FilterOption[]> {
    return this.referentiel.getNiveaux().pipe(map((n) => toLevelFilters(n)));
  }
}
