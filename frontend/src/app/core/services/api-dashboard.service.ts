import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardActivityDto, DashboardStatsDto } from '../api/api-dto';
import { mapDashboardStats, toSchoolLevels } from '../api/api-mappers';
import { ReferentielApiService } from '../api/referentiel-api.service';
import { DashboardStat } from '../models/dashboard-stats.model';
import { SchoolLevel } from '../models/school-level.model';
import { Transaction, TransactionStatus } from '../models/transaction.model';
import { DashboardDataProvider } from './interfaces/dashboard-data-provider.interface';

@Injectable({ providedIn: 'root' })
export class ApiDashboardService implements DashboardDataProvider {
  private readonly http = inject(HttpClient);
  private readonly referentiel = inject(ReferentielApiService);
  private readonly base = environment.apiUrl;

  getSchoolLevels(): Observable<readonly SchoolLevel[]> {
    return this.referentiel.getNiveaux().pipe(map((n) => toSchoolLevels(n)));
  }

  getStats(levelId: string | null): Observable<readonly DashboardStat[]> {
    const options = levelId ? { params: { niveauId: levelId } } : undefined;
    return this.http
      .get<DashboardStatsDto>(`${this.base}/dashboard/stats`, options)
      .pipe(map((dto) => mapDashboardStats(dto)));
  }

  getTransactions(levelId: string | null): Observable<readonly Transaction[]> {
    const options = levelId ? { params: { niveauId: levelId } } : undefined;
    return this.http
      .get<DashboardActivityDto[]>(`${this.base}/dashboard/activity`, options)
      .pipe(map((items) => items.map((dto) => this.mapActivity(dto))));
  }

  private mapActivity(dto: DashboardActivityDto): Transaction {
    return {
      reference: dto.reference,
      parentName: dto.parentName,
      studentsCount: dto.studentsCount,
      level: dto.level,
      amount: Number(dto.amount),
      date: dto.date,
      status: this.mapActivityStatus(dto.status),
    };
  }

  private mapActivityStatus(status: string): TransactionStatus {
    const normalized = status?.toLowerCase();
    if (normalized === 'paid') return 'paid';
    if (normalized === 'pending') return 'pending';
    if (normalized === 'overdue') return 'overdue';
    return 'pending';
  }
}
