import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { combineLatest, map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EleveDto } from '../api/api-dto';
import { mapStudent, toClassFilters, toLevelFilters } from '../api/api-mappers';
import { ReferentielApiService } from '../api/referentiel-api.service';
import { FilterOption } from '../models/filter-option.model';
import { StudentsListQuery } from '../models/list-query.model';
import { Student } from '../models/student.model';
import { StudentsDataProvider } from './interfaces/students-data-provider.interface';

@Injectable({ providedIn: 'root' })
export class ApiStudentsService implements StudentsDataProvider {
  private readonly http = inject(HttpClient);
  private readonly referentiel = inject(ReferentielApiService);
  private readonly base = environment.apiUrl;

  getStudents(query?: StudentsListQuery): Observable<readonly Student[]> {
    const params: Record<string, string> = {};
    if (query?.search?.trim()) params['search'] = query.search.trim();
    if (query?.niveauId && query.niveauId !== 'all') params['niveauId'] = query.niveauId;
    if (query?.classeId && query.classeId !== 'all') params['classeId'] = query.classeId;
    return combineLatest([
      this.http.get<EleveDto[]>(`${this.base}/eleves`, { params }),
      this.referentiel.getClasses(),
    ]).pipe(
      map(([eleves, classes]) => {
        const classByLabel = new Map(classes.map((c) => [c.nom, c]));
        return eleves.map((dto) => {
          const cls = classByLabel.get(dto.classe);
          return {
            ...mapStudent(dto, query?.niveauId ?? ''),
            classId: cls ? String(cls.id) : '',
          };
        });
      })
    );
  }

  getLevelFilters(): Observable<readonly FilterOption[]> {
    return this.referentiel.getNiveaux().pipe(map((n) => toLevelFilters(n)));
  }

  getClassFilters(niveauId?: string): Observable<readonly FilterOption[]> {
    return this.referentiel
      .getClasses(niveauId)
      .pipe(map((c) => toClassFilters(c)));
  }
}
