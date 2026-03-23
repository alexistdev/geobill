import {Component, Inject, OnDestroy, OnInit, PLATFORM_ID, signal} from '@angular/core';
import {CommonModule, DecimalPipe, isPlatformBrowser} from '@angular/common';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {Footer} from '../../share/footer/footer';
import {Header} from '../../share/header/header';
import {Menutop} from '../../share/menutop/menutop';
import {InvoiceServiceApi} from '../invoice-component/service/invoice-service-api.service';
import {InvoiceModel} from '../orderhosting/model/invoice.model';
import {CustomerModel} from '../orderhosting/model/customer.model';
import {InvoiceResponseModel} from '../invoice-component/model/response/invoice-response.model';
import {Api_base_response_single} from '../../share/response/apiresponsesingle';
declare var Lobibox: any;

@Component({
  selector: 'app-invoice-detail-component',
  imports: [
    Footer,
    Header,
    Menutop,
    CommonModule,
    RouterModule
  ],
  templateUrl: './invoice-detail-component.html',
  styleUrl: './invoice-detail-component.css',
  providers: [InvoiceServiceApi]
})
export class InvoiceDetailComponent implements OnInit, OnDestroy {

  homeLink = '/users/billings';
  protected invoice = signal<InvoiceResponseModel | undefined>(undefined);


  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private route: ActivatedRoute,
    private router: Router,
    private invoiceServiceApi: InvoiceServiceApi
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.route.params.subscribe(params => {
        const uuid = params['uuid'];
        if (uuid) {
          this.loadInvoiceDetail(uuid);
        }
      });
    }
  }

  loadInvoiceDetail(uuid: string): void {
    this.invoiceServiceApi.getInvoiceDetail(uuid).subscribe({
      next: (data: Api_base_response_single<InvoiceResponseModel>) => {
        const invoiceResult = data.payload;
        console.log('Invoice:', invoiceResult);
        this.invoice.set(invoiceResult);
      },
      error: (err) => {
        console.error('Error fetching invoice details:', err);
        this.LobiboxMessage('error', 'Failed to load invoice details', 'bx bx-x-circle');
      }
    });
  }

  LobiboxMessage(type: string, msg: string, icon: string): void {
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
      console.warn('Lobibox is not defined. Ensure it is loaded correctly.');
    }
  }

  ngOnDestroy(): void {
  }
}
