import {Component, inject, OnInit, PLATFORM_ID, signal} from '@angular/core';
import {Footer} from "../../share/footer/footer";
import {Header} from "../../share/header/header";
import {Menutop} from "../../share/menutop/menutop";
import {InvoiceResponseModel} from './model/response/invoice-response.model';
import {Router, RouterLink} from '@angular/router';
import {DecimalPipe, isPlatformBrowser} from '@angular/common';
import {Apiresponse} from '../../share/response/apiresponse';
import {InvoiceServiceApi} from './service/invoice-service-api.service';

@Component({
  selector: 'app-invoice-component',
  imports: [
    Footer,
    Header,
    Menutop,
    DecimalPipe,
    RouterLink
  ],
  templateUrl: './invoice-component.html',
  styleUrl: './invoice-component.css',
  providers: [InvoiceServiceApi]
})
export class InvoiceComponent implements OnInit {
  protected invoices = signal<InvoiceResponseModel[]>([]);
  protected payload? = signal<InvoiceResponseModel | undefined>(undefined);
  protected totalData = signal<number>(0);
  protected pageNumber = signal<number>(0);
  protected totalPages = signal<number>(0);
  protected pageSize = signal<number>(10);
  protected keyword: string = "";
  protected searchQuery: string = '';
  private platformId = inject(PLATFORM_ID);

  constructor(
    private invoiceServiceApi:InvoiceServiceApi,
    private router: Router,
  ) { }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadData(this.pageNumber(), this.pageSize());
    }
  }

  loadData(page: number, size: number = 10): void {
    this.pageNumber.set(page);
    this.pageSize.set(size);
    this.keyword = this.searchQuery;
    const sortBy = 'createdDate';
    const direction = 'desc';
    const isFiltering = this.keyword !== "";

    const request$ = isFiltering ?
      this.invoiceServiceApi.getAllInvoices(this.pageNumber(), this.pageSize(), sortBy, direction):
      this.invoiceServiceApi.getAllInvoices(this.pageNumber(), this.pageSize(), sortBy, direction);

    request$.subscribe({
      next: (data) => this.updateInvoicePageData(data),
      error: (err) => {
        if (err.message === 'Session expired') {
          this.router.navigate(['/login']).then(() => console.log('Redirected to login'));
        }
      }
    });
  }

  private updateInvoicePageData(data: Apiresponse<InvoiceResponseModel>) {
    const payloadData = data.payload;
    this.totalData.set(data.payload?.totalElements ?? 0);
    this.pageNumber.set(data.payload.pageable.pageNumber ?? 0);
    this.totalPages.set(data.payload.totalPages ?? 0);
    this.pageSize.set(data.payload.pageable.pageSize ?? 0);
    this.invoices.set(payloadData?.content ?? []);
  }

}
