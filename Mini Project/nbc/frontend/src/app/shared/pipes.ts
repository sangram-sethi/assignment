import { Pipe, PipeTransform } from '@angular/core';
import {
  formatCompactINR,
  formatDate,
  formatDateTime,
  formatINR,
  relativeTime,
} from './format';

@Pipe({ name: 'inr', standalone: true })
export class InrPipe implements PipeTransform {
  transform(value: number | null | undefined, decimals = false): string {
    return formatINR(value, decimals);
  }
}

@Pipe({ name: 'inrCompact', standalone: true })
export class InrCompactPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    return formatCompactINR(value);
  }
}

@Pipe({ name: 'shortDate', standalone: true })
export class ShortDatePipe implements PipeTransform {
  transform(value: string | Date | null | undefined): string {
    return formatDate(value);
  }
}

@Pipe({ name: 'dateTime', standalone: true })
export class DateTimePipe implements PipeTransform {
  transform(value: string | Date | null | undefined): string {
    return formatDateTime(value);
  }
}

@Pipe({ name: 'ago', standalone: true })
export class AgoPipe implements PipeTransform {
  transform(value: string | Date | null | undefined): string {
    return relativeTime(value);
  }
}
