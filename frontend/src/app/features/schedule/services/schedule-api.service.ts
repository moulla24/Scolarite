import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SeanceEmploiDto, SeanceRequest } from '../../../core/api/api-dto';

@Injectable({ providedIn: 'root' })
export class ScheduleApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  list(classeId?: string, semaineId?: string): Observable<readonly SeanceEmploiDto[]> {
    const params: Record<string, string> = {};
    if (classeId && classeId !== 'all') params['classeId'] = classeId;
    if (semaineId) params['semaineId'] = semaineId;
    return this.http.get<SeanceEmploiDto[]>(`${this.base}/emploi-du-temps`, { params });
  }

  getById(id: number): Observable<SeanceEmploiDto> {
    return this.http.get<SeanceEmploiDto>(`${this.base}/emploi-du-temps/${id}`);
  }

  create(request: SeanceRequest): Observable<SeanceEmploiDto> {
    return this.http.post<SeanceEmploiDto>(`${this.base}/emploi-du-temps`, request);
  }

  update(id: number, request: SeanceRequest): Observable<SeanceEmploiDto> {
    return this.http.put<SeanceEmploiDto>(`${this.base}/emploi-du-temps/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/emploi-du-temps/${id}`);
  }
}
