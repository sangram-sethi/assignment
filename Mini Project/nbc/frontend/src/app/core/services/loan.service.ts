import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../api.config';
import {
  EmiCalculationRequest,
  EmiCalculationResponse,
  LoanApplicationRequest,
  LoanApplicationResponse,
  LoanType,
} from '../models';

@Injectable({ providedIn: 'root' })
export class LoanService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_BASE);

  getTypes(): Observable<LoanType[]> {
    return this.http.get<LoanType[]>(`${this.api}/loans/types`);
  }

  calculateEmi(req: EmiCalculationRequest): Observable<EmiCalculationResponse> {
    return this.http.post<EmiCalculationResponse>(`${this.api}/loans/calculate-emi`, req);
  }

  apply(req: LoanApplicationRequest): Observable<LoanApplicationResponse> {
    return this.http.post<LoanApplicationResponse>(`${this.api}/loans`, req);
  }

  getMyLoans(): Observable<LoanApplicationResponse[]> {
    return this.http.get<LoanApplicationResponse[]>(`${this.api}/loans`);
  }

  getLoan(id: number): Observable<LoanApplicationResponse> {
    return this.http.get<LoanApplicationResponse>(`${this.api}/loans/${id}`);
  }
}
