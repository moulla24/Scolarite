import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ClasseDto, ClasseRequest } from '../../../core/api/api-dto';

@Injectable({ providedIn: 'root' })
export class ClassroomsApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/classes`;

  getById(id: number): Observable<ClasseDto> {
    return this.http.get<ClasseDto>(`${this.base}/${id}`);
  }

  create(body: ClasseRequest): Observable<ClasseDto> {
    return this.http.post<ClasseDto>(this.base, body);
  }

  update(id: number, body: ClasseRequest): Observable<ClasseDto> {
    return this.http.put<ClasseDto>(`${this.base}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
