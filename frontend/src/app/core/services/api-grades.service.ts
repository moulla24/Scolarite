import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BulletinEleveDto } from '../api/api-dto';
import { mapGradeRecord, toClassFilters, toLevelFilters, toSubjectFilters } from '../api/api-mappers';
import { ReferentielApiService } from '../api/referentiel-api.service';
import { FilterOption } from '../models/filter-option.model';
import { GradesListQuery } from '../models/list-query.model';
import { GradeRecord } from '../models/grade-record.model';
import { GradesDataProvider } from './interfaces/grades-data-provider.interface';

@Injectable({ providedIn: 'root' })
export class ApiGradesService implements GradesDataProvider {
  private readonly http = inject(HttpClient);
  private readonly referentiel = inject(ReferentielApiService);
  private readonly base = environment.apiUrl;

  getGradeRecords(query?: GradesListQuery): Observable<readonly GradeRecord[]> {
    const params: Record<string, string> = {};
    if (query?.classeId && query.classeId !== 'all') params['classeId'] = query.classeId;
    if (query?.niveauId && query.niveauId !== 'all') params['niveauId'] = query.niveauId;
    return this.http.get<BulletinEleveDto[]>(`${this.base}/notes/bulletins`, { params }).pipe(
      map((list) =>
        list.map((dto) => ({
          ...mapGradeRecord(dto, query?.niveauId ?? ''),
          classId: query?.classeId && query.classeId !== 'all' ? query.classeId : '',
        }))
      )
    );
  }

  getClassFilters(niveauId?: string): Observable<readonly FilterOption[]> {
    return this.referentiel
      .getClasses(niveauId)
      .pipe(map((c) => toClassFilters(c)));
  }

  getSubjectFilters(): Observable<readonly FilterOption[]> {
    return this.referentiel.getMatieres().pipe(map((m) => toSubjectFilters(m)));
  }

  getLevelFilters(): Observable<readonly FilterOption[]> {
    return this.referentiel.getNiveaux().pipe(map((n) => toLevelFilters(n)));
  }
}
