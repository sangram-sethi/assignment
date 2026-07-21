/**
 * Domain models mirroring the Spring Boot DTOs and enums exposed by the NBC API.
 * Kept as a single source of truth for the whole frontend.
 */

export type Role = 'CUSTOMER' | 'LOAN_APPROVER';

export type LoanStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export type LoanType =
  | 'HOME_LOAN'
  | 'PERSONAL_LOAN'
  | 'VEHICLE_LOAN'
  | 'EDUCATION_LOAN'
  | 'BUSINESS_LOAN';

/* -------------------------------------------------------------- requests */

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterCustomerRequest {
  name: string;
  email: string;
  phone: string;
  annualIncome: number;
  username: string;
  password: string;
}

export interface LoanApplicationRequest {
  loanType: LoanType;
  loanAmount: number;
  tenureMonths: number;
  purpose: string;
}

export interface EmiCalculationRequest {
  loanType: LoanType;
  loanAmount: number;
  tenureMonths: number;
}

export interface LoanApprovalRequest {
  status: Extract<LoanStatus, 'APPROVED' | 'REJECTED'>;
  remarks: string;
}

/* ------------------------------------------------------------- responses */

export interface JwtResponse {
  token: string;
  username: string;
  role: Role;
}

export interface CustomerResponse {
  id: number;
  name: string;
  email: string;
  phone: string;
  annualIncome: number;
}

export interface LoanSummaryResponse {
  totalApplications: number;
  approvedApplications: number;
  rejectedApplications: number;
  pendingApplications: number;
}

export interface LoanApplicationResponse {
  id: number;
  loanType: LoanType;
  loanAmount: number;
  tenureMonths: number;
  purpose: string;
  interestRate: number;
  monthlyEmi: number;
  totalPayable: number;
  totalInterest: number;
  applicationDate: string;
  status: LoanStatus;
  remarks: string | null;
  approvedBy: string | null;
}

export interface EmiCalculationResponse {
  loanType: LoanType;
  loanAmount: number;
  tenureMonths: number;
  annualInterestRate: number;
  monthlyEmi: number;
  totalPayable: number;
  totalInterest: number;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string | string[];
  path: string;
}

/** The authenticated principal we persist client-side. */
export interface AuthUser {
  username: string;
  role: Role;
  token: string;
}
