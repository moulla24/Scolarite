import {

  ChangeDetectionStrategy,

  Component,

  inject,

  signal,

} from '@angular/core';

import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Router } from '@angular/router';

import { HttpErrorResponse } from '@angular/common/http';

import { APP_NAME } from '../../../../core/constants/app.constants';

import { AuthService } from '../../../../core/auth/auth.service';



@Component({

  selector: 'app-login-page',

  standalone: true,

  imports: [ReactiveFormsModule],

  templateUrl: './login-page.component.html',

  styleUrl: './login-page.component.scss',

  changeDetection: ChangeDetectionStrategy.OnPush,

})

export class LoginPageComponent {

  private readonly auth = inject(AuthService);

  private readonly router = inject(Router);

  private readonly fb = inject(FormBuilder);



  readonly appName = APP_NAME;

  readonly showPassword = signal(false);

  readonly loading = signal(false);

  readonly errorMessage = signal<string | null>(null);



  readonly form = this.fb.nonNullable.group({

    email: ['', [Validators.required, Validators.email]],

    password: ['', Validators.required],

  });



  onSubmit(): void {

    this.form.markAllAsTouched();

    if (this.form.invalid) {

      this.errorMessage.set('Veuillez renseigner l’e-mail et le mot de passe.');

      return;

    }



    const { email, password } = this.form.getRawValue();

    this.loading.set(true);

    this.errorMessage.set(null);



    this.auth.login(email.trim(), password).subscribe({

      next: (user) => {

        this.loading.set(false);

        const target = user.role?.toUpperCase() === 'COMPTABLE' ? '/comptabilite' : '/dashboard';

        void this.router.navigate([target]);

      },

      error: (err: unknown) => {

        this.loading.set(false);

        this.errorMessage.set(this.resolveErrorMessage(err));

      },

    });

  }



  togglePassword(): void {

    this.showPassword.update((v) => !v);

  }



  clearError(): void {

    this.errorMessage.set(null);

  }



  private resolveErrorMessage(err: unknown): string {

    if (err instanceof HttpErrorResponse) {

      const body = err.error as { message?: string } | null;

      const serverMsg = body?.message?.trim();



      if (err.status === 0) {

        return 'Impossible de joindre le serveur. Démarrez le backend (port 8081) puis réessayez.';

      }

      if (err.status === 400 || err.status === 401 || err.status === 403) {

        return serverMsg || 'Identifiants incorrects. Vérifiez votre e-mail et votre mot de passe.';

      }

      if (serverMsg) {

        return serverMsg;

      }

    }

    return 'Échec de la connexion. Réessayez.';

  }

}


