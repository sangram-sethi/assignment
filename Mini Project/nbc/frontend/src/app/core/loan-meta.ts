import { LoanType, LoanStatus } from './models';

export interface LoanTypeMeta {
  type: LoanType;
  label: string;
  short: string;
  icon: string;
  rate: number;
  blurb: string;
  gradient: string;
  glow: string;
}

/**
 * Presentation metadata for each loan type. Interest rates mirror the
 * server-side {@code LoanType} enum but the authoritative figures always
 * come from the API responses.
 */
export const LOAN_TYPES: Record<LoanType, LoanTypeMeta> = {
  HOME_LOAN: {
    type: 'HOME_LOAN',
    label: 'Home Loan',
    short: 'Home',
    icon: 'home',
    rate: 8.5,
    blurb: 'Build or buy your dream home with our lowest rates.',
    gradient: 'linear-gradient(135deg, #7c5cff, #b06bff)',
    glow: 'rgba(124, 92, 255, 0.45)',
  },
  PERSONAL_LOAN: {
    type: 'PERSONAL_LOAN',
    label: 'Personal Loan',
    short: 'Personal',
    icon: 'spark',
    rate: 12.0,
    blurb: 'Flexible funds for whatever life throws your way.',
    gradient: 'linear-gradient(135deg, #ff6ea9, #f472b6)',
    glow: 'rgba(255, 110, 169, 0.45)',
  },
  VEHICLE_LOAN: {
    type: 'VEHICLE_LOAN',
    label: 'Vehicle Loan',
    short: 'Vehicle',
    icon: 'car',
    rate: 9.5,
    blurb: 'Drive home your next car or bike, hassle-free.',
    gradient: 'linear-gradient(135deg, #38bdf8, #22d3ee)',
    glow: 'rgba(56, 189, 248, 0.45)',
  },
  EDUCATION_LOAN: {
    type: 'EDUCATION_LOAN',
    label: 'Education Loan',
    short: 'Education',
    icon: 'cap',
    rate: 10.0,
    blurb: 'Invest in a future without limits.',
    gradient: 'linear-gradient(135deg, #34d399, #10b981)',
    glow: 'rgba(52, 211, 153, 0.45)',
  },
  BUSINESS_LOAN: {
    type: 'BUSINESS_LOAN',
    label: 'Business Loan',
    short: 'Business',
    icon: 'briefcase',
    rate: 11.5,
    blurb: 'Fuel your ambition and scale with confidence.',
    gradient: 'linear-gradient(135deg, #fbbf24, #f59e0b)',
    glow: 'rgba(251, 191, 36, 0.45)',
  },
};

export const LOAN_TYPE_LIST: LoanTypeMeta[] = Object.values(LOAN_TYPES);

export function loanMeta(type: LoanType | string): LoanTypeMeta {
  return LOAN_TYPES[type as LoanType] ?? LOAN_TYPES.PERSONAL_LOAN;
}

export interface StatusMeta {
  label: string;
  icon: string;
  className: string;
  color: string;
}

export const STATUS_META: Record<LoanStatus, StatusMeta> = {
  PENDING: { label: 'Pending', icon: 'clock', className: 'badge-pending', color: '#fbbf24' },
  APPROVED: { label: 'Approved', icon: 'check', className: 'badge-approved', color: '#34d399' },
  REJECTED: { label: 'Rejected', icon: 'x', className: 'badge-rejected', color: '#fb7185' },
};

export function statusMeta(status: LoanStatus | string): StatusMeta {
  return STATUS_META[status as LoanStatus] ?? STATUS_META.PENDING;
}
