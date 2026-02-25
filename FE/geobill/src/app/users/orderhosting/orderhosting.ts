/*
 * Copyright (c) 2026.
 * Project : GeoBill
 * Author : Alexsander Hendra Wijaya
 * Github : https://github.com/alexistdev
 * Email : alexistdev@gmail.com
 */

import {Component, inject, OnChanges, OnInit, PLATFORM_ID} from '@angular/core';
import {Footer} from '../../share/footer/footer';
import {Header} from '../../share/header/header';
import {Menutop} from '../../share/menutop/menutop';
import {Topheader} from '../../share/topheader/topheader';
import {Producttypemodel} from '../../admin/masterdata/producttype/producttypemodel.model';
import {Producttypeservice} from '../../admin/masterdata/producttype/producttypeservice';
import {isPlatformBrowser} from '@angular/common';
import {Orderhostingservice} from './orderhostingservice';

@Component({
  selector: 'app-orderhosting',
  imports: [
    Footer,
    Menutop,
    Topheader
  ],
  templateUrl: './orderhosting.html',
  styleUrl: './orderhosting.css',
})
export class Orderhosting implements OnInit, OnChanges {

  protected productTypes: Producttypemodel[] = [];
  private platformId = inject(PLATFORM_ID);
  private productTypesLoaded = false;

  constructor(private orderhostingservice: Orderhostingservice) {
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
      },
      error: (err) => console.log(err)
    });
  }
}
