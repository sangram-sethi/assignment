import jsPDF from 'jspdf';
import { LoanApplicationResponse } from '../core/models';
import { loanMeta } from '../core/loan-meta';
import { amortizationSchedule } from './finance';

const money = (n: number): string =>
  'Rs. ' + new Intl.NumberFormat('en-IN', { maximumFractionDigits: 2 }).format(Number(n ?? 0));

const date = (v: string | null | undefined): string =>
  v
    ? new Date(v).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
    : '—';

/** Generates and downloads a branded PDF statement for a loan application. */
export function exportLoanPdf(loan: LoanApplicationResponse, applicant?: string): void {
  const doc = new jsPDF({ unit: 'pt', format: 'a4' });
  const W = doc.internal.pageSize.getWidth();
  const H = doc.internal.pageSize.getHeight();
  const M = 40;
  const meta = loanMeta(loan.loanType);

  const header = (): void => {
    doc.setFillColor(12, 16, 40);
    doc.rect(0, 0, W, 92, 'F');
    doc.setFillColor(124, 92, 255);
    doc.rect(0, 90, W, 3, 'F');
    // brand mark
    doc.setFillColor(124, 92, 255);
    doc.roundedRect(M, 26, 40, 40, 9, 9, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(22);
    doc.text('A', M + 20, 53, { align: 'center' });
    doc.setFontSize(16);
    doc.text('Aurora Loan Studio', M + 54, 44);
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.setTextColor(180, 188, 220);
    doc.text('Loan Statement', M + 54, 60);
    doc.setTextColor(180, 188, 220);
    doc.setFontSize(9);
    doc.text(`Ref #${loan.id}`, W - M, 44, { align: 'right' });
    doc.text(date(loan.applicationDate), W - M, 60, { align: 'right' });
  };

  let y = 120;

  const sectionTitle = (t: string): void => {
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(11);
    doc.setTextColor(20, 24, 54);
    doc.text(t, M, y);
    doc.setDrawColor(226, 230, 244);
    doc.line(M, y + 6, W - M, y + 6);
    y += 22;
  };

  const row = (label: string, value: string, right = false): void => {
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    doc.setTextColor(110, 118, 150);
    doc.text(label, right ? W / 2 + 10 : M, y);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(20, 24, 54);
    doc.text(value, right ? W - M : W / 2 - 10, y, { align: 'right' });
  };

  header();

  // status pill
  doc.setFillColor(...statusColor(loan.status));
  doc.roundedRect(M, y - 12, 74, 20, 10, 10, 'F');
  doc.setTextColor(255, 255, 255);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(9);
  doc.text(loan.status, M + 37, y + 2, { align: 'center' });
  doc.setTextColor(20, 24, 54);
  doc.setFontSize(15);
  doc.text(meta.label, M + 90, y + 3);
  y += 34;

  sectionTitle('Applicant & Loan');
  row('Applicant', applicant || '—');
  row('Loan type', meta.label, true);
  y += 18;
  row('Purpose', trunc(loan.purpose, 40));
  row('Tenure', `${loan.tenureMonths} months`, true);
  y += 26;

  sectionTitle('Financial Summary');
  row('Principal amount', money(loan.loanAmount));
  row('Interest rate', `${loan.interestRate}% p.a.`, true);
  y += 18;
  row('Monthly EMI', money(loan.monthlyEmi));
  row('Total interest', money(loan.totalInterest), true);
  y += 18;
  row('Total payable', money(loan.totalPayable));
  if (loan.approvedBy) row('Reviewed by', loan.approvedBy, true);
  y += 28;

  // amortization schedule
  sectionTitle('Repayment Schedule');
  const schedule = amortizationSchedule(
    Number(loan.loanAmount),
    Number(loan.interestRate),
    Number(loan.tenureMonths),
  );
  const cols = [
    { t: '#', x: M, w: 34, align: 'left' as const },
    { t: 'EMI', x: M + 90, w: 90, align: 'right' as const },
    { t: 'Principal', x: M + 210, w: 90, align: 'right' as const },
    { t: 'Interest', x: M + 330, w: 90, align: 'right' as const },
    { t: 'Balance', x: W - M, w: 90, align: 'right' as const },
  ];

  const drawTableHead = (): void => {
    doc.setFillColor(244, 246, 252);
    doc.rect(M, y - 12, W - 2 * M, 20, 'F');
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(9);
    doc.setTextColor(90, 98, 130);
    doc.text('#', M + 4, y + 2);
    doc.text('EMI', M + 170, y + 2, { align: 'right' });
    doc.text('Principal', M + 290, y + 2, { align: 'right' });
    doc.text('Interest', M + 400, y + 2, { align: 'right' });
    doc.text('Balance', W - M - 4, y + 2, { align: 'right' });
    y += 22;
  };

  drawTableHead();
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(8.5);
  for (const r of schedule) {
    if (y > H - 50) {
      footer(doc, W, H);
      doc.addPage();
      y = 60;
      drawTableHead();
      doc.setFont('helvetica', 'normal');
      doc.setFontSize(8.5);
    }
    doc.setTextColor(120, 128, 158);
    doc.text(String(r.month), M + 4, y);
    doc.setTextColor(30, 36, 66);
    doc.text(money(r.emi).replace('Rs. ', ''), M + 170, y, { align: 'right' });
    doc.text(money(r.principal).replace('Rs. ', ''), M + 290, y, { align: 'right' });
    doc.text(money(r.interest).replace('Rs. ', ''), M + 400, y, { align: 'right' });
    doc.text(money(r.balance).replace('Rs. ', ''), W - M - 4, y, { align: 'right' });
    y += 15;
  }

  footer(doc, W, H);
  doc.save(`Aurora-Loan-${loan.id}-${meta.short}.pdf`);
}

function footer(doc: jsPDF, W: number, H: number): void {
  doc.setDrawColor(226, 230, 244);
  doc.line(40, H - 34, W - 40, H - 34);
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(8);
  doc.setTextColor(150, 156, 182);
  doc.text('Generated by Aurora Loan Studio · Amounts in INR (Rs.)', 40, H - 20);
  const page = doc.getNumberOfPages();
  doc.text(`Page ${page}`, W - 40, H - 20, { align: 'right' });
}

function statusColor(status: string): [number, number, number] {
  if (status === 'APPROVED') return [16, 185, 129];
  if (status === 'REJECTED') return [239, 68, 68];
  return [245, 158, 11];
}

function trunc(s: string | null | undefined, n: number): string {
  if (!s) return '—';
  return s.length > n ? s.slice(0, n - 1) + '…' : s;
}
