import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { EnseignantDto, EnseignantRequest } from '../../../core/api/api-dto';

@Injectable({ providedIn: 'root' })
export class TeachersApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/enseignants`;

  getById(id: number): Observable<EnseignantDto> {
    return this.http.get<EnseignantDto>(`${this.base}/${id}`);
  }

  create(body: EnseignantRequest): Observable<EnseignantDto> {
    return this.http.post<EnseignantDto>(this.base, body);
  }

  update(id: number, body: EnseignantRequest): Observable<EnseignantDto> {
    return this.http.put<EnseignantDto>(`${this.base}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
