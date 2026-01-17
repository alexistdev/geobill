/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import {Component, OnInit, ChangeDetectorRef, PLATFORM_ID, inject, ViewChild, ElementRef} from '@angular/core';
import { Menutop } from '../../../share/menutop/menutop';
import { Payload } from '../../../share/response/payload';
import { Producttypemodel } from './producttypemodel.model';
import { Producttypeservice } from './producttypeservice';
import { Apiresponse } from '../../../share/response/apiresponse';
import {Router} from '@angular/router';
import {CommonModule, DatePipe, isPlatformBrowser} from '@angular/common';
import {Pagination} from '../../../share/pagination/pagination';
import {debounceTime, distinctUntilChanged, Subject} from 'rxjs';
import {Producttypemodal} from './producttypemodal/producttypemodal';
import {Producttyperequest} from './producttyperequest.model';
declare var Lobibox: any;

@Component({
  selector: 'app-producttype',
  imports: [
    CommonModule,
    Menutop,
    Pagination,
    Producttypemodal
  ],
  templateUrl: './producttype.html',
  styleUrl: './producttype.css',
  providers: [DatePipe]
})
export class Producttype implements OnInit {

  // @ViewChild('addButton', { static: true }) addButton!: ElementRef<HTMLButtonElement>;

  @ViewChild('addButton', { static: false }) addButton?: ElementRef<HTMLButtonElement>;

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

  private searchSubject = new Subject<string>();

  currentEditMode: boolean = false;
  selectedProductTypeId: number | undefined = 0;
  private platformId = inject(PLATFORM_ID);

  protected readonly Number = Number;

  constructor(
    private producttypeservice: Producttypeservice,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private datePipe: DatePipe,
    private el: ElementRef
  ) {
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadData(this.pageNumber);
    }
    //set delay for request sent to backend
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe((searchTerm:string) => {
      this.searchQuery = searchTerm.toLowerCase();
      this.loadData(this.pageNumber);
    });
  }

  loadData(page: number, size: number = 10): void {
    this.pageNumber = page;
    this.pageSize = size;
    this.keyword = this.searchQuery;
    const sortBy = 'createdDate';
    const direction = 'desc';
    const isFiltering = this.keyword !== "";

    const request$ = isFiltering ?
      this.producttypeservice.getProductTypeByFilter(this.keyword, this.pageNumber, this.pageSize, sortBy, direction) :
      this.producttypeservice.getProductType(this.pageNumber, this.pageSize, sortBy, direction);

    request$.subscribe({
      next: (data) => this.updateProductTypePageData(data),
      error: (err) => {
        if (err.message === 'Session expired') {
          console.warn('User session ended. Redirecting...');
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

    const newItems =  this.payload.content.map(producttypemodel => {
      return {
        ...producttypemodel,
        name: this.capitalizeWords(producttypemodel.name),
        createdDate: producttypemodel.createdDate ? this.datePipe.transform(producttypemodel.createdDate,'dd-MM-yyyy HH:mm:ss'): '',
        modifiedDate: producttypemodel.modifiedDate ? this.datePipe.transform(producttypemodel.modifiedDate,'dd-MM-yyyy HH:mm:ss'): ''
      };
    });

    this.producttypes = [...newItems];

    this.cdr.markForCheck();
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
    this.searchSubject.next(searchTerm);
  }

  isNumber(value: any): boolean {
    return typeof value === 'number';
  }

  onPageSizeChange(event: any) {
    this.pageSize = parseInt(event.target.value, 10);
    this.loadData(0, this.pageSize);
  }

  onPageChanged(page: number) {
    this.pageNumber = page;
    this.loadData(this.pageNumber, this.pageSize);
  }

  openModal(type: 'form' | 'confirm', data?: any, productTypeId?: number) {
    this.selectedProductTypeId = productTypeId;
    this.currentModalType = type;
    this.showModal = true;
    if (type === 'form') {
      this.currentFormData = data || {};
    } else {
      this.currentConfirmationText = data || 'Are you sure you want to proceed?';
    }
  }

  closeModal() {
    this.el.nativeElement.blur();
    this.showModal = false;
  }

  doSaveData(formValue: Producttyperequest  & { id?: number }){
    const request : Producttyperequest & { id? : number } = {
      name: formValue.name,
      id: formValue.id
    }
    this.producttypeservice.saveProductType(request).subscribe({
      next: () => {
        if(isPlatformBrowser(this.platformId)){
          this.LobiboxMessage('success', 'Data berhasil disimpan');
        }
        this.closeModal();
        this.loadData(this.pageNumber, this.pageSize);
      },
      error: (err) => {
        console.error(err);
      }
    })
  }

  openEditModal(productType: any) {
    this.showModal = true;
    this.currentModalType = 'form';
    this.currentFormData = {...productType};
    this.currentEditMode = true;
    this.currentConfirmationText = '';
  }

  onDeleteConfirm(){
    console.log("posisi: 10 - onDeleteConfirm");
    this.closeModal();
  }

  LobiboxMessage(type: string, msg: string):void {
    if (typeof Lobibox !== 'undefined') {
      Lobibox.notify(type, {
        pauseDelayOnHover: true,
        continueDelayOnInactiveTab: false,
        position: 'top right',
        msg: msg
      });
    } else {
      console.warn('Lobibox is not defined.Ensure it is loaded correctly.');
    }
  }
}
