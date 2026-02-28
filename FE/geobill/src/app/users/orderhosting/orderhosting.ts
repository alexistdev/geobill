/*
 * Copyright (c) 2026.
 * Project : GeoBill
 * Author : Alexsander Hendra Wijaya
 * Github : https://github.com/alexistdev
 * Email : alexistdev@gmail.com
 */

import { ChangeDetectorRef, Component, inject, OnChanges, OnInit, PLATFORM_ID } from '@angular/core';
import { Footer } from '../../share/footer/footer';
import { Header } from '../../share/header/header';
import { Menutop } from '../../share/menutop/menutop';
import { Topheader } from '../../share/topheader/topheader';
import { Producttypemodel } from '../../admin/masterdata/producttype/producttypemodel.model';
import { Producttypeservice } from '../../admin/masterdata/producttype/producttypeservice';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Orderhostingservice } from './orderhostingservice';
import {Productmodel} from '../../admin/masterdata/productcomponent/productmodel.model';
import {Payload} from '../../share/response/payload';
import {Apiresponse} from '../../share/response/apiresponse';
import {Router, RouterLink} from '@angular/router';
import {ToNumberPipe} from './tonumberpipe';

@Component({
  selector: 'app-orderhosting',
  imports: [
    CommonModule,
    Footer,
    Menutop,
    Topheader,
    RouterLink,
    ToNumberPipe
  ],
  templateUrl: './orderhosting.html',
  styleUrl: './orderhosting.css',
})
export class Orderhosting implements OnInit, OnChanges {
  protected products: Productmodel[]=[];
  protected payload?: Payload<Productmodel>;
  protected pageNumber: number = 0;
  protected totalPages: number = 0;
  protected pageSize: number = 0;
  protected size: number = 10;
  protected idSearch: string = "";
  homeLink = '/users/dashboard';

  protected productTypes: Producttypemodel[] = [];
  private platformId = inject(PLATFORM_ID);
  private productTypesLoaded = false;

  constructor(
    private orderhostingservice: Orderhostingservice,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    this.loadProductType();
  }

  ngOnChanges(): void {
  }

  loadProductType(): void {
    if (!isPlatformBrowser(this.platformId) || this.productTypesLoaded) {
      return;
    }

    this.orderhostingservice.getProductType(0, 100, 'name', 'asc').subscribe({
      next: (data) => {
        this.productTypes = data.payload.content;
        this.productTypesLoaded = true;
        this.cdr.detectChanges();
      },
      error: (err) => console.log(err)
    });
  }

  loadDataProduct(id: string) {

    this.orderhostingservice.getProductByProductTypeId(id, 0, 100, 'name', 'asc').subscribe({
      next: (data) => {
        this.updateProductPageData(data);
      },
      error: (err) => {
        if (err.message === 'Session expired') {
          console.warn('User session ended. Redirecting...');
          this.router.navigate(['/login']);
        } else {
          console.error(err);
        }
      }
    })
  }

  onSelect(event: Event) {
    const selectElement = event.target as HTMLSelectElement;
    const selectedId = selectElement.value;
    if (selectedId) {
      this.loadDataProduct(selectedId);
    }
  }

  private updateProductPageData(data: Apiresponse<Productmodel>) {
    let newItems = [];
      this.payload = data.payload;
    this.pageNumber = data.payload.number;
    this.totalPages = data.payload.totalPages;
    this.pageSize = data.payload.size;
    this.products = data.payload.content;

    newItems = this.payload.content.map(productmodel => {
      return {
        ...productmodel
      };
    });
    this.products = [...newItems];
    this.cdr.detectChanges();
  }

  protected readonly String = String;


  goBuyNow(uuid: string) {
    this.router.navigate(['/users/services/order/v'], { queryParams: {id : uuid} });
  }
}
