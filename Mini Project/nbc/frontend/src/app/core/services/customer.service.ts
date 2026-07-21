import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../api.config';
import { CustomerResponse, LoanSummaryResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_BASE);

  getProfile(): Observable<CustomerResponse> {
    return this.http.get<CustomerResponse>(`${this.api}/customers/profile`);
  }

  getLoanSummary(): Observable<LoanSummaryResponse> {
    return this.http.get<LoanSummaryResponse>(`${this.api}/customers/loan-summary`);
  }
}
