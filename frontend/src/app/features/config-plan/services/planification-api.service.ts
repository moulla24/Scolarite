import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  QuotaControleDto,
  QuotasControlesRequest,
  SemaineDto,
  TrimestreDto,
  TrimestreRequest,
  TypeControleDto,
  TypeControleRequest,
} from '../../../core/api/api-dto';

@Injectable({ providedIn: 'root' })
export class PlanificationApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  listSemaines(trimestreId: number): Observable<readonly SemaineDto[]> {
    return this.http.get<SemaineDto[]>(
      `${this.base}/planification/trimestres/${trimestreId}/semaines`
    );
  }

  listTrimestres(anneeScolaireId?: number): Observable<readonly TrimestreDto[]> {
    const params: Record<string, string> = {};
    if (anneeScolaireId) params['anneeScolaireId'] = String(anneeScolaireId);
    return this.http.get<TrimestreDto[]>(`${this.base}/planification/trimestres`, { params });
  }

  createTrimestre(request: TrimestreRequest): Observable<TrimestreDto> {
    return this.http.post<TrimestreDto>(`${this.base}/planification/trimestres`, request);
  }

  updateTrimestre(id: number, request: TrimestreRequest): Observable<TrimestreDto> {
    return this.http.put<TrimestreDto>(`${this.base}/planification/trimestres/${id}`, request);
  }

  deleteTrimestre(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/planification/trimestres/${id}`);
  }

  listTypesControles(niveauId?: number): Observable<readonly TypeControleDto[]> {
    const params: Record<string, string> = {};
    if (niveauId) params['niveauId'] = String(niveauId);
    return this.http.get<TypeControleDto[]>(`${this.base}/planification/types-controles`, { params });
  }

  createTypeControle(request: TypeControleRequest): Observable<TypeControleDto> {
    return this.http.post<TypeControleDto>(`${this.base}/planification/types-controles`, request);
  }

  updateTypeControle(id: number, request: TypeControleRequest): Observable<TypeControleDto> {
    return this.http.put<TypeControleDto>(`${this.base}/planification/types-controles/${id}`, request);
  }

  deleteTypeControle(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/planification/types-controles/${id}`);
  }

  listQuotasControles(trimestreId: number, niveauId: number): Observable<readonly QuotaControleDto[]> {
    return this.http.get<QuotaControleDto[]>(`${this.base}/planification/quotas-controles`, {
      params: { trimestreId: String(trimestreId), niveauId: String(niveauId) },
    });
  }

  saveQuotasControles(request: QuotasControlesRequest): Observable<readonly QuotaControleDto[]> {
    return this.http.put<QuotaControleDto[]>(`${this.base}/planification/quotas-controles`, request);
  }
}
