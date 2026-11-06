export type StatTrend = 'up' | 'down' | 'neutral';

export interface DashboardStat {
  readonly id: string;
  readonly title: string;
  readonly value: string;
  readonly detail: string;
  readonly icon: string;
  readonly accent: 'primary' | 'blue' | 'violet' | 'teal' | 'danger' | 'success' | 'warning';
  readonly trend?: StatTrend;
}
