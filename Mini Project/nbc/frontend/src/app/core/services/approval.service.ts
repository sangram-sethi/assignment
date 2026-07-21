import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../api.config';
import { LoanApplicationResponse, LoanApprovalRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class ApprovalService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_BASE);

  getPending(): Observable<LoanApplicationResponse[]> {
    return this.http.get<LoanApplicationResponse[]>(`${this.api}/approvals/pending`);
  }

  getPendingCount(): Observable<number> {
    return this.http.get<number>(`${this.api}/approvals/pending/count`);
  }

  decide(loanId: number, req: LoanApprovalRequest): Observable<LoanApplicationResponse> {
    return this.http.put<LoanApplicationResponse>(`${this.api}/approvals/${loanId}`, req);
  }
}
