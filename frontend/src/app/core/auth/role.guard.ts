import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, take } from 'rxjs';
import { AuthService } from './auth.service';

export function roleGuard(...allowedRoles: string[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    return auth.currentUser$.pipe(
      take(1),
      map((user) => {
        if (!user) {
          return router.createUrlTree(['/login']);
        }
        const role = user.role?.toUpperCase() ?? '';
        if (allowedRoles.map((r) => r.toUpperCase()).includes(role)) {
          return true;
        }
        if (role === 'COMPTABLE') {
          return router.createUrlTree(['/comptabilite']);
        }
        return router.createUrlTree(['/dashboard']);
      })
    );
  };
}
