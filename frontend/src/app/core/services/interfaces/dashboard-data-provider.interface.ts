import { Observable } from 'rxjs';
import { DashboardStat } from '../../models/dashboard-stats.model';
import { SchoolLevel } from '../../models/school-level.model';
import { Transaction } from '../../models/transaction.model';

export interface DashboardDataProvider {
  getStats(levelId: string | null): Observable<readonly DashboardStat[]>;
  getTransactions(levelId: string | null): Observable<readonly Transaction[]>;
  getSchoolLevels(): Observable<readonly SchoolLevel[]>;
}
