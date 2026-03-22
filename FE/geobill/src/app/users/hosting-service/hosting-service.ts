import {Component, inject, OnInit, PLATFORM_ID, signal} from '@angular/core';
import {Menutop} from "../../share/menutop/menutop";
import {Footer} from '../../share/footer/footer';
import {Header} from '../../share/header/header';
import {HostingResponseModel} from './model/response/hosting-response.model';
import {Payload} from '../../share/response/payload';
import {HostingServiceApi} from './service/hosting-service-api.service';
import {Router} from '@angular/router';
import {Apiresponse} from '../../share/response/apiresponse';
import {DecimalPipe, isPlatformBrowser} from '@angular/common';

@Component({
  selector: 'app-hosting-service',
  imports: [
    Menutop,
    Footer,
    Header,
    DecimalPipe,
  ],
  templateUrl: './hosting-service.html',
  styleUrl: './hosting-service.css'
})
export class HostingService implements OnInit {
    hostings = signal<HostingResponseModel[]>([]);
    payload = signal<Payload<HostingResponseModel> | undefined>(undefined);
    totalData = signal<number>(0);
    pageNumber = signal<number>(0);
    totalPages = signal<number>(0);
    pageSize = signal<number>(10);
    keyword: string = "";
    searchQuery: string = '';
    private platformId = inject(PLATFORM_ID);

  constructor(
    private hostingServiceApi:HostingServiceApi,
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
        this.hostingServiceApi.getAllHostingService(this.pageNumber(), this.pageSize(), sortBy, direction):
        this.hostingServiceApi.getAllHostingService(this.pageNumber(), this.pageSize(), sortBy, direction);

      request$.subscribe({
        next: (data) => this.updateHostingPageData(data),
        error: (err) => {
          if (err.message === 'Session expired') {
            this.router.navigate(['/login']).then(() => console.log('Redirected to login'));
          }
        }
      });
  }

  private updateHostingPageData(data: Apiresponse<HostingResponseModel>) {
    this.payload.set(data.payload);
    this.totalData.set(data.payload?.totalElements ?? 0);
    this.pageNumber.set(data.payload.pageable.pageNumber);
    this.totalPages.set(data.payload.totalPages);
    this.pageSize.set(data.payload.pageable.pageSize);
    this.hostings.set([...data.payload.content]);
  }

}
