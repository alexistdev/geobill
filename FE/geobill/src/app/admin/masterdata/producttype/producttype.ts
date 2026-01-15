import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Menutop } from '../../../share/menutop/menutop';
import { Searchmodal } from '../../../share/searchmodal/searchmodal';
import { Payload } from '../../../share/response/payload';
import { Producttypemodel } from './producttypemodel.model';
import { Producttypeservice } from './producttypeservice';
import { Apiresponse } from '../../../share/response/apiresponse';
import {Router} from '@angular/router';
import {CredentialEncryptedService} from '../../../utils/auth/credential-encrypted.service';

@Component({
  selector: 'app-producttype',
  imports: [
    Menutop,
    Searchmodal
  ],
  templateUrl: './producttype.html',
  styleUrl: './producttype.css'
})
export class Producttype implements OnInit {
  producttypes: Producttypemodel[] = [];
  payload?: Payload<Producttypemodel>
  totalData: number = 0;
  pageNumber: number = 0;
  totalPages: number = 0;
  pageSize: number = 0;
  keyword: string = "";
  searchQuery: string = '';

  public showModal = false;
  public currentModalType: 'form' | 'confirm' = 'confirm';
  public currentFormData: any = {};
  public currentConfirmationText = '';

  currentEditMode: boolean = false;
  selectedProvinceId: number | undefined = 0;

  protected readonly Number = Number;

  constructor(
    private producttypeservice: Producttypeservice,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.loadData(this.pageNumber);
  }

  loadData(page: number, size: number = 10): void {
    this.pageNumber = page;
    this.pageSize = size;
    this.keyword = this.searchQuery;
    const sortBy = 'id';
    const direction = 'desc';
    const isFiltering = this.keyword !== "";

    const request$ = isFiltering ?
      this.producttypeservice.getProductTypeByFilter(this.keyword, this.pageNumber, this.pageSize, sortBy, direction) :
      this.producttypeservice.getProductType(this.pageNumber, this.pageSize, sortBy, direction);

    request$.subscribe({
      next: (data) => this.updateProductTypePageData(data),
      error: (err) => {
        if (err.message === 'Session expired') {
          console.warn('User session ended. Redirecting...2');
          this.router.navigate(['/login']);
        }  else {
          console.error(err);
        }
      }
    });
  }

  private updateProductTypePageData(data: Apiresponse<Producttypemodel>) {
    this.payload = data.payload;
    this.totalData = this.payload?.totalElements ?? 0;
    this.pageNumber = this.payload.pageable.pageNumber;
    this.totalPages = this.payload.totalPages;
    this.pageSize = this.payload.pageable.pageSize;

    this.producttypes = this.payload.content.map(producttypemodel => {
      return {
        ...producttypemodel,
        name: this.capitalizeWords(producttypemodel.name),
        createdDate: producttypemodel.createdDate,
        modifiedDate: producttypemodel.modifiedDate
      };
    });
    this.cdr.detectChanges();
  }

  private capitalizeWords(input: string): string {
    return input.split(' ').map(word => {
      return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
    }).join(' ');
  }

  onSearchChange(searchTerm: string) {
    if (this.pageNumber > 0) {
      this.pageNumber = 0;
    }

    this.searchQuery = searchTerm.toLowerCase();
    this.loadData(this.pageNumber, this.pageSize);
  }

  isNumber(value: any): boolean {
    return typeof value === 'number';
  }

  onPageSizeChange() {
    this.loadData(0, this.pageSize);
  }

  onPageChanged(page: number) {
    this.pageNumber = page;
    this.loadData(this.pageNumber, this.pageSize);
  }

  openModal(type: 'form' | 'confirm', data?: any, provinceId?: number) {
    this.selectedProvinceId = provinceId;
    this.currentModalType = type;
    this.showModal = true;
    if (type === 'form') {
      this.currentFormData = data || {};
    } else {
      this.currentConfirmationText = data || 'Are you sure you want to proceed?';
    }
  }

  closeModal() {
    this.showModal = false;
  }

}
