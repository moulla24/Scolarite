import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { EleveDto, EleveRequest, TuteurDto } from '../../../core/api/api-dto';
import { FilterOption } from '../../../core/models/filter-option.model';

@Injectable({ providedIn: 'root' })
export class StudentsApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/eleves`;

  readonly defaultEcoleId = 2;

  getById(id: number): Observable<EleveDto> {
    return this.http.get<EleveDto>(`${this.base}/${id}`);
  }

  getParentOptions(): Observable<readonly FilterOption[]> {
    return this.http.get<TuteurDto[]>(`${environment.apiUrl}/parents`).pipe(
      map((parents) =>
        parents.map((p) => ({
          id: String(p.id),
          label: p.nomComplet,
        }))
      )
    );
  }

  create(body: EleveRequest): Observable<EleveDto> {
    return this.http.post<EleveDto>(this.base, body);
  }

  update(id: number, body: EleveRequest): Observable<EleveDto> {
    return this.http.put<EleveDto>(`${this.base}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
