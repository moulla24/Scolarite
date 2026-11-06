import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TuteurDto } from '../api/api-dto';
import { mapParent, toLevelFilters } from '../api/api-mappers';
import { ReferentielApiService } from '../api/referentiel-api.service';
import { FilterOption } from '../models/filter-option.model';
import { ParentsListQuery } from '../models/list-query.model';
import { Parent } from '../models/parent.model';
import { ParentsDataProvider } from './interfaces/parents-data-provider.interface';

@Injectable({ providedIn: 'root' })
export class ApiParentsService implements ParentsDataProvider {
  private readonly http = inject(HttpClient);
  private readonly referentiel = inject(ReferentielApiService);
  private readonly base = environment.apiUrl;

  getParents(query?: ParentsListQuery): Observable<readonly Parent[]> {
    const params: Record<string, string> = {};
    if (query?.search?.trim()) params['search'] = query.search.trim();
    if (query?.niveauId && query.niveauId !== 'all') params['niveauId'] = query.niveauId;
    if (query?.statutCompte && query.statutCompte !== 'all') params['statutCompte'] = query.statutCompte;
    return this.http.get<TuteurDto[]>(`${this.base}/parents`, { params }).pipe(
      map((list) =>
        list.map((dto) => mapParent(dto, query?.niveauId && query.niveauId !== 'all' ? query.niveauId : ''))
      )
    );
  }

  getSchoolLevelFilters(): Observable<readonly FilterOption[]> {
    return this.referentiel.getNiveaux().pipe(map((n) => toLevelFilters(n)));
  }
}
