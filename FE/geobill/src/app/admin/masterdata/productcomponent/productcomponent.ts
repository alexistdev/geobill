import {ChangeDetectorRef, Component, ElementRef, inject, NgZone, OnInit, PLATFORM_ID} from '@angular/core';
import {CommonModule, DatePipe, isPlatformBrowser} from '@angular/common';
import {Menutop} from '../../../share/menutop/menutop';
import {debounceTime, distinctUntilChanged, Subject} from 'rxjs';
import {Router} from '@angular/router';
import {Productmodel} from './productmodel.model';
import {Payload} from '../../../share/response/payload';
import {Productservice} from './productservice';
import {Apiresponse} from '../../../share/response/apiresponse';
import {Productmodal} from './productmodal/productmodal';
import {Producttypemodel} from '../producttype/producttypemodel.model';
import {Producttypeservice} from '../producttype/producttypeservice';
import {Productrequest} from './productrequest.model';
declare var Lobibox: any;

@Component({
  selector: 'app-productcomponent',
  imports: [
    CommonModule,
    Menutop,
    Productmodal
  ],
  templateUrl: './productcomponent.html',
  styleUrl: './productcomponent.css',
  providers: [DatePipe]
})
export class Productcomponent implements OnInit{
  products: Productmodel[] = [];
  productTypes: Producttypemodel[] = [];
  payload?: Payload<Productmodel>;
  totalData: number = 0;
  pageNumber: number = 0;
  totalPages: number = 0;
  pageSize: number = 10;
  keyword: string = "";
  searchQuery: string = '';

  public showModal = false;
  public currentModalType: 'form' | 'confirm' = 'confirm';
  public currentFormData: any = {};
  public currentConfirmationText = '';
  private searchSubject = new Subject<string>();
  currentEditMode: boolean = false;
  selectedProductTypeId: string | undefined = '';
  private platformId = inject(PLATFORM_ID);
  protected readonly Number = Number;

  constructor(
    private productservice: Productservice,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private datePipe: DatePipe,
    private el: ElementRef,
    private ngZone: NgZone
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadData(this.pageNumber, this.pageSize);
    }

    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe((searchTerm:string) => {

      this.searchQuery = searchTerm.toLowerCase();
      console.log( this.searchQuery);
      this.loadData(this.pageNumber,this.pageSize);
    });
  }

  openModal(type: 'form' | 'confirm', data?: any, productTypeId?: string) {
    this.selectedProductTypeId = productTypeId;
    this.currentModalType = type;
    this.showModal = true;
    this.currentFormData = data || {};
  }

  openEditModal(product: any) {
    this.showModal = true;
    this.currentModalType = 'form';
    this.currentFormData = {...product};
    this.currentEditMode = true;
    this.currentConfirmationText = '';
  }

  openDeleteModal(productType: any) {
    this.showModal = true;
    this.currentModalType = 'confirm';
    this.currentConfirmationText = 'Are you sure you want to delete this product type?';
    this.selectedProductTypeId = productType.id;
  }

  loadData(page: number, size: number = 10): void {
    this.pageNumber = page;
    this.pageSize = size;
    this.keyword = this.searchQuery;
    const sortBy = 'createdDate';
    const direction = 'desc';
    const isFiltering = this.keyword !== "";

    const request$ = isFiltering ?
      this.productservice.getProductByFilter(this.keyword, this.pageNumber, this.pageSize, sortBy, direction) :
      this.productservice.getProduct(this.pageNumber, this.pageSize, sortBy, direction);

    request$.subscribe({
      next: (data) => this.updateProductPageData(data),
      error: (err) => {
        if (err.message === 'Session expired') {
          console.warn('User session ended. Redirecting...');
          this.router.navigate(['/login']);
        }  else {
          // console.error("wakanda");
          // console.error(err);
        }
      }
    });


  }

  private updateProductPageData(data: Apiresponse<Productmodel>) {
    // console.log(data.payload.content);
    this.payload = data.payload;
    this.totalData = this.payload?.totalElements ?? 0;
    this.pageNumber = this.payload.pageable.pageNumber;
    this.totalPages = this.payload.totalPages;
    this.pageSize = this.payload.pageable.pageSize;

    const newItems = this.payload.content.map(productmodel => {
      return {
        ...productmodel,
        name: this.capitalizeWords(productmodel.name)
      };
    });
    this.products = [...newItems];
    this.cdr.markForCheck();
    this.cdr.detectChanges();
  }

  private capitalizeWords(input: string): string {
    return input.split(' ').map(word => {
      return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
    }).join(' ');
  }

  onSearchChange(searchTerm: string) {
    console.log('input event:', searchTerm);
    if (this.pageNumber > 0) {
      this.pageNumber = 0;
    }
    this.searchSubject.next(searchTerm);
  }

  onDeleteConfirm(){

  }

  doSaveData(formValue: Productrequest & { id?: number }){
    const isUpdate = !!formValue.id;

    const apiCall = formValue.id
      ? this.productservice.updateProduct(formValue)
      : this.productservice.saveProduct(formValue);

    apiCall.subscribe({
      next: () => {
        if(isPlatformBrowser(this.platformId)){
          this.LobiboxMessage(isUpdate ? 'warning' : 'success', isUpdate ? 'Data berhasil diubah' : 'Data berhasil disimpan',
            isUpdate ? 'bx bx-check-circle' : 'bx bx-check-circle');
        }
        this.closeModal();
        this.loadData(this.pageNumber, this.pageSize);
      },
      error: (err) => {
        let errorMessage = 'An unexpected error occurred.';

        try {
          console.error(err);
          errorMessage = err.error?.messages?.[0] || errorMessage;
        } catch (e){
          console.error('Error while processing error:', e);
        }
        this.LobiboxMessage('error', errorMessage,'bx bx-x-circle');
        this.ngZone.run(() => {
          this.closeModal();
        });
      }
    })
  }

  closeModal(){
    this.el.nativeElement.blur();
    this.showModal = false;
    this.cdr.detectChanges();
  }

  LobiboxMessage(type: string, msg: string, icon: string):void {
    if (typeof Lobibox !== 'undefined') {
      Lobibox.notify(type, {
        pauseDelayOnHover: true,
        size: 'mini',
        rounded: true,
        delayIndicator: false,
        icon: icon,
        continueDelayOnInactiveTab: false,
        position: 'top right',
        msg: msg
      });
    } else {
      console.warn('Lobibox is not defined.Ensure it is loaded correctly.');
    }
  }

}
