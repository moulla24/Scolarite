import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { EleveDto, TuteurDto, TuteurRequest } from '../../../core/api/api-dto';

@Injectable({ providedIn: 'root' })
export class ParentsApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/parents`;

  getById(id: number): Observable<TuteurDto> {
    return this.http.get<TuteurDto>(`${this.base}/${id}`);
  }

  create(body: TuteurRequest): Observable<TuteurDto> {
    return this.http.post<TuteurDto>(this.base, body);
  }

  update(id: number, body: TuteurRequest): Observable<TuteurDto> {
    return this.http.put<TuteurDto>(`${this.base}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  listEleves(niveauId: string, classeId: string): Observable<EleveDto[]> {
    const params: Record<string, string> = {};
    if (niveauId) params['niveauId'] = niveauId;
    if (classeId) params['classeId'] = classeId;
    return this.http.get<EleveDto[]>(`${environment.apiUrl}/eleves`, { params });
  }

  listAllEleves(): Observable<EleveDto[]> {
    return this.http.get<EleveDto[]>(`${environment.apiUrl}/eleves`);
  }
}
