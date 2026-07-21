/** Reducing-balance EMI math — mirrors the server-side EmiCalculator. */

export interface AmortRow {
  month: number;
  emi: number;
  principal: number;
  interest: number;
  balance: number;
}

/** Monthly EMI for a reducing-balance loan. */
export function calcEmi(principal: number, annualRatePct: number, months: number): number {
  if (principal <= 0 || months <= 0) return 0;
  const r = annualRatePct / 12 / 100;
  if (r === 0) return principal / months;
  const pow = Math.pow(1 + r, months);
  return (principal * r * pow) / (pow - 1);
}

export interface EmiTotals {
  emi: number;
  totalPayable: number;
  totalInterest: number;
}

export function emiTotals(principal: number, annualRatePct: number, months: number): EmiTotals {
  const emi = calcEmi(principal, annualRatePct, months);
  const totalPayable = emi * months;
  return {
    emi,
    totalPayable,
    totalInterest: Math.max(0, totalPayable - principal),
  };
}

/** Full amortization schedule (one row per month). */
export function amortizationSchedule(
  principal: number,
  annualRatePct: number,
  months: number,
): AmortRow[] {
  const rows: AmortRow[] = [];
  const r = annualRatePct / 12 / 100;
  const emi = calcEmi(principal, annualRatePct, months);
  let balance = principal;
  for (let m = 1; m <= months; m++) {
    const interest = balance * r;
    let principalPaid = emi - interest;
    if (m === months || principalPaid > balance) principalPaid = balance;
    balance = Math.max(0, balance - principalPaid);
    rows.push({ month: m, emi, principal: principalPaid, interest, balance });
  }
  return rows;
}
