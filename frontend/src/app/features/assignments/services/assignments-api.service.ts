import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { EnseignementDto, EnseignementRequest } from '../../../core/api/api-dto';

@Injectable({ providedIn: 'root' })
export class AssignmentsApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  list(classeId?: string, matiereId?: string): Observable<readonly EnseignementDto[]> {
    const params: Record<string, string> = {};
    if (classeId && classeId !== 'all') params['classeId'] = classeId;
    if (matiereId && matiereId !== 'all') params['matiereId'] = matiereId;
    return this.http.get<EnseignementDto[]>(`${this.base}/enseignements`, { params });
  }

  getById(id: number): Observable<EnseignementDto> {
    return this.http.get<EnseignementDto>(`${this.base}/enseignements/${id}`);
  }

  create(request: EnseignementRequest): Observable<EnseignementDto> {
    return this.http.post<EnseignementDto>(`${this.base}/enseignements`, request);
  }

  update(id: number, request: EnseignementRequest): Observable<EnseignementDto> {
    return this.http.put<EnseignementDto>(`${this.base}/enseignements/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/enseignements/${id}`);
  }
}
