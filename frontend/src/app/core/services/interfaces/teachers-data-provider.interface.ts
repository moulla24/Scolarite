import { Observable } from 'rxjs';
import { FilterOption } from '../../models/filter-option.model';
import { TeachersListQuery } from '../../models/list-query.model';
import { Teacher } from '../../models/teacher.model';

export interface TeachersDataProvider {
  getTeachers(query?: TeachersListQuery): Observable<readonly Teacher[]>;
  getSubjectFilters(): Observable<readonly FilterOption[]>;
}
