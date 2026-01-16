import {Component, Input, Output, EventEmitter} from '@angular/core';
import {CommonModule} from '@angular/common';


@Component({
  selector: 'app-pagination',
  imports: [CommonModule],
  templateUrl: './pagination.html',
  styleUrl: './pagination.css',
})
export class Pagination {
  @Input() pageNumber: number = 0;
  @Input() totalPages: number = 1;
  @Input() pageSize: number = 10;
  @Output() pageChange = new EventEmitter<number>();

  getDisplayedPages(): (number | string)[] {
    const pages: (number | string)[] = [];
    const total = this.totalPages;
    const current = this.pageNumber;

    if (total <= 10) {
      for (let i = 1; i <= total; i++) {
        pages.push(i);
      }
    } else if (current <= 9) {
      pages.push(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, '...');
    } else if (current >= total - 2) {
      pages.push('...', total - 5, total - 4, total - 3, total - 2, total - 1, total);
    } else {
      pages.push('...', current, current + 1, current + 2, current + 3, current + 4, '...');
    }

    return pages;
  }

  isNumber(value: any): boolean {
    return typeof value === 'number';
  }

  onPageClick(page: number) {
    if (page >= 0 && page < this.totalPages && page !== this.pageNumber) {
      this.pageChange.emit(page);
    }
  }

  protected readonly Number = Number;
}
