import { Observable } from 'rxjs';
import { FilterOption } from '../../models/filter-option.model';
import { StudentsListQuery } from '../../models/list-query.model';
import { Student } from '../../models/student.model';

export interface StudentsDataProvider {
  getStudents(query?: StudentsListQuery): Observable<readonly Student[]>;
  getLevelFilters(): Observable<readonly FilterOption[]>;
  getClassFilters(niveauId?: string): Observable<readonly FilterOption[]>;
}
