export type TransactionStatus = 'paid' | 'pending' | 'overdue';

export interface Transaction {
  readonly reference: string;
  readonly parentName: string;
  readonly studentsCount: number;
  readonly level: string;
  readonly amount: number;
  readonly date: string;
  readonly status: TransactionStatus;
}
