import {ChangeDetectorRef, Component, ElementRef, inject, Inject, OnDestroy, OnInit, PLATFORM_ID} from '@angular/core';
import { Footer } from "../../../share/footer/footer";
import { Header } from "../../../share/header/header";
import { Menutop } from "../../../share/menutop/menutop";
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Orderhostingservice } from '../orderhostingservice';
import { DecimalPipe, isPlatformBrowser, NgClass } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { ToNumberPipe } from '../tonumberpipe';
import {Checkoutmodal} from './checkoutmodal/checkoutmodal';
import {Localstorageservice} from '../../../utils/localstorage/localstorageservice';

@Component({
  selector: 'app-checkout',
  imports: [
    Footer,
    Header,
    Menutop,
    RouterLink,
    NgClass,
    FormsModule,
    ToNumberPipe,
    DecimalPipe,
    Checkoutmodal
  ],
  templateUrl: './checkout.html',
  styleUrl: './checkout.css',
})
export class Checkout implements OnInit, OnDestroy {
  productId: string | null = null;
  productName: string | null = null;
  storage: string | null = null;
  bandwith: string | null = null;
  price: string | null = null;
  database: string | null = null;
  email_account: string | null = null;
  ftp_account: string | null = null;
  domain: string | null = null;
  info1: string | null = null;
  info2: string | null = null;
  info3: string | null = null;
  info4: string | null = null;
  info5: string | null = null;
  homeLink = '/users/services/order';
  inputDomainName: string = '';
  isCheckoutPageLoading: boolean = false;
  isInvoicePageLoading: boolean = false;
  isDomainValid: boolean = false;
  isSubmitted: boolean = false;
  originalPrice: string | null = null;
  showModal = false;
  orderCycle: number = 1;
  priceOrdered: number = 0;

  private readonly destroy$ = new Subject<void>();
  private platformId = inject(PLATFORM_ID);


  constructor(
    private route: ActivatedRoute,
    private orderhostingservice: Orderhostingservice,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private el: ElementRef,
    private localStorageService: Localstorageservice,
  ) { }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
        const productIdentity = params['id'] ? params['id'] : null;
        this.productId = productIdentity ? productIdentity : '';
        this.loadDetailProduct(this.productId);
      })
    }
  }

  changeCycle(selectedValue: any):void {
    this.price = this.originalPrice;
    let numericPrice = Number(this.price);

    if(isNaN(numericPrice)) {
      console.log("Invalid price value:", this.price);
      numericPrice = 0;
    }

    let numericCycle = Number(selectedValue);
    this.orderCycle = numericCycle;
    if(isNaN(numericCycle)) {
      console.log("Invalid cycle value:", selectedValue);
      numericCycle = 0;
    }
    var tempPrice = numericCycle * numericPrice;
    this.price = tempPrice.toString();
    this.priceOrdered = tempPrice;
    this.cdr.detectChanges();
  }

  loadDetailProduct(id: string | null): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    if (id == null) {
      return;
    }

    this.orderhostingservice.getProductById(id).subscribe({
      next: (data) => {
        const product = data.payload as any;
        this.productName = product.name;
        this.price = product.price.toString();
        this.storage = product.capacity;
        this.bandwith = product.bandwith;
        this.database = product.database_account;
        this.email_account = product.email_account || "Unlimited";
        this.ftp_account = product.ftp_account;
        this.info1 = product.info1;
        this.info2 = product.info2;
        this.info3 = product.info3;
        this.info4 = product.info4;
        this.info5 = product.info5;
        this.originalPrice = product.price.toString();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
      }
    })
  }

  goToCheckout() {
    this.isSubmitted = true;
    this.validateDomain();

    if (!this.isDomainValid) {
      return;
    }

    this.isCheckoutPageLoading = true;
    this.router.navigate(['/users/services/order/v'], { queryParams: { id: this.productId, domain: this.inputDomainName } });
  }

  validateDomain() {
    if (!this.inputDomainName) {
      this.isDomainValid = false;
      return;
    }

    if (this.inputDomainName.startsWith('www.')) {
      this.inputDomainName = this.inputDomainName.slice(4);
    }

    const domainRegex = /^[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/;
    this.isDomainValid = domainRegex.test(this.inputDomainName);

    if (!this.isDomainValid) {
      console.log('Invalid domain format.');
    }
  }

  openModal() {
    console.log("test");
    this.showModal = true;
    this.cdr.detectChanges();
  }

  closeModal() {
    this.el.nativeElement.blur();
    this.showModal = false;
    this.cdr.detectChanges();
  }

  saveData() {
    this.showModal = false;
    let userId:string = this.localStorageService.getItem("userId");
    console.log("User ID:", userId);
    console.log("Domain Name:", this.inputDomainName);
    console.log("Product ID:", this.productId);
    console.log("Price:", this.price);
    console.log("cycle:", this.orderCycle);
    this.isCheckoutPageLoading = false;
    this.isInvoicePageLoading = true;
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

}
