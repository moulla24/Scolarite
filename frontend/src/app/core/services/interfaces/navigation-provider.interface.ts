import { Observable } from 'rxjs';
import { NavigationItem } from '../../models/navigation-item.model';
import { UserProfile } from '../../models/user.model';

export interface NavigationProvider {
  getMenuItems(): Observable<readonly NavigationItem[]>;
  getCurrentUser(): Observable<UserProfile>;
}
