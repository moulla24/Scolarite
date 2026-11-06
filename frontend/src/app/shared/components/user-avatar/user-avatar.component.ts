import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-user-avatar',
  standalone: true,
  template: `<span class="avatar" [attr.aria-label]="name()">{{ initials() }}</span>`,
  styles: `
    .avatar {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 2.25rem;
      height: 2.25rem;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--color-accent-blue-light) 0%, var(--color-accent-blue) 55%, var(--color-primary) 100%);
      color: #fff;
      box-shadow: 0 2px 8px rgba(99, 197, 218, 0.35);
      font-size: 0.7rem;
      font-weight: 700;
      flex-shrink: 0;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserAvatarComponent {
  readonly initials = input.required<string>();
  readonly name = input('');
}
