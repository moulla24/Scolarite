import { Observable } from 'rxjs';
import { FilterOption } from '../../models/filter-option.model';
import { GradesListQuery } from '../../models/list-query.model';
import { GradeRecord } from '../../models/grade-record.model';

export interface GradesDataProvider {
  getGradeRecords(query?: GradesListQuery): Observable<readonly GradeRecord[]>;
  getClassFilters(niveauId?: string): Observable<readonly FilterOption[]>;
  getSubjectFilters(): Observable<readonly FilterOption[]>;
  getLevelFilters(): Observable<readonly FilterOption[]>;
}
