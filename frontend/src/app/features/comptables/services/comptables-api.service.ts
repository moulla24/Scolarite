import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ComptableDto, ComptableRequest } from '../../../core/api/api-dto';

export interface Comptable {
  readonly id: string;
  readonly fullName: string;
  readonly initials: string;
  readonly email: string;
  readonly phone: string;
  readonly matricule: string;
  readonly dateEmbauche: string | null;
  readonly status: string;
}

@Injectable({ providedIn: 'root' })
export class ComptablesApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/comptables`;

  list(search?: string): Observable<readonly Comptable[]> {
    const params: Record<string, string> = {};
    if (search?.trim()) params['search'] = search.trim();
    return this.http
      .get<ComptableDto[]>(this.base, { params })
      .pipe(map((list) => list.map((dto) => this.map(dto))));
  }

  getById(id: number): Observable<ComptableDto> {
    return this.http.get<ComptableDto>(`${this.base}/${id}`);
  }

  create(body: ComptableRequest): Observable<ComptableDto> {
    return this.http.post<ComptableDto>(this.base, body);
  }

  update(id: number, body: ComptableRequest): Observable<ComptableDto> {
    return this.http.put<ComptableDto>(`${this.base}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  private map(dto: ComptableDto): Comptable {
    return {
      id: String(dto.id),
      fullName: dto.nomComplet,
      initials: dto.initials,
      email: dto.email ?? '',
      phone: dto.telephone ?? '',
      matricule: dto.matricule ?? '',
      dateEmbauche: dto.dateEmbauche ?? null,
      status: dto.statut,
    };
  }
}
