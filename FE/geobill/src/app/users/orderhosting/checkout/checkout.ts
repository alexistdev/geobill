import {Component, Inject, OnDestroy, OnInit, PLATFORM_ID} from '@angular/core';
import {Footer} from "../../../share/footer/footer";
import {Header} from "../../../share/header/header";
import {Menutop} from "../../../share/menutop/menutop";
import {ActivatedRoute} from '@angular/router';
import {Orderhostingservice} from '../orderhostingservice';
import {isPlatformBrowser} from '@angular/common';
import {Subject, takeUntil} from 'rxjs';

@Component({
  selector: 'app-checkout',
    imports: [
        Footer,
        Header,
        Menutop
    ],
  templateUrl: './checkout.html',
  styleUrl: './checkout.css',
})
export class Checkout implements OnInit,OnDestroy{
  productId: string | null = null;
  private readonly destroy$ = new Subject<void>();


  constructor(private route:ActivatedRoute,
              private orderhostingservice: Orderhostingservice,
              @Inject(PLATFORM_ID) private platformId: Object) {
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
        const productIdentity = params['id'] ? params['id'] : null;
        this.productId = productIdentity? productIdentity : '';
        console.log("wakanda" + this.productId);
      })
    }

  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }






}
