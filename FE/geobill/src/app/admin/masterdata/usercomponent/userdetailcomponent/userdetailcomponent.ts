/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import { Component, OnInit, PLATFORM_ID, Inject, ChangeDetectorRef, signal, WritableSignal } from '@angular/core';
import {CommonModule, DatePipe, isPlatformBrowser} from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Menutop } from '../../../../share/menutop/menutop';
import { Footer } from '../../../../share/footer/footer';
import { Userdetailservice } from './userdetailservice';
import { Topheader } from '../../../../share/topheader/topheader';
import {Userdetailrequest} from './userdetailrequest.model';
declare var Lobibox: any;

@Component({
  selector: 'app-userdetailcomponent',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, Menutop, Footer, Topheader,DatePipe],
  templateUrl: './userdetailcomponent.html',
  styleUrl: './userdetailcomponent.css',
})
export class Userdetailcomponent implements OnInit {
  homeLink = '/admin/dashboard';
  userEmail: string = 'User Profile';
  userIdentity: string = '';
  isEditMode: boolean = false;

  Email: string = '';
  FullName: string = '';
  PhoneNumber: string = '';
  Address1: string = '';
  Address2: string = '';
  BusinessName: string = '';
  City: string = '';
  State: string = '';
  PostalCode: string = '';
  Country: string = '';
  createdDate: string = '';

  originalEmail: string = '';
  originalFullName: string = '';
  originalPhoneNumber: string = '';
  originalAddress1: string = '';
  originalAddress2: string = '';
  originalBusinessName: string = '';
  originalCity: string = '';
  originalState: string = '';
  originalPostalCode: string = '';
  originalCountry: string = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private userDetailService: Userdetailservice,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void {
    this.homeLink = this.router.getCurrentNavigation()?.extras.state?.['homeLink'] || '/admin/users';
    this.userEmail = this.router.getCurrentNavigation()?.extras.state?.['userEmail'] || 'User Profile';

    if (isPlatformBrowser(this.platformId)) {
      this.route.paramMap.subscribe(params => {
        const userId = params.get('uuid');
        this.userIdentity = userId? userId : '';
        if (userId) {
          this.loadUserDetails(userId);
        } else {
          this.LobiboxMessage('error', 'Please try again later...', 'bx bx-error');
          console.warn('UserDetailComponent: No UUID found in route paramMap');
        }
      })
    }
  }

  loadUserDetails(userId: string): void {
    const request$ = this.userDetailService.getUsersDetail(userId);
    request$.subscribe({
      next: (data) => {
        if (data.payload) {
          const userDetail: any = data.payload;
          const customer: any = userDetail.customer;
          this.Email = userDetail.email;
          this.FullName = userDetail.fullName;
          this.PhoneNumber = customer.phone;
          this.Address1 = customer.address1;
          this.Address2 = customer.address2;
          this.City = customer.city;
          this.State = customer.state;
          this.PostalCode = customer.postCode;
          this.Country = customer.country;
          this.BusinessName = customer.businessName;
          this.createdDate = userDetail.createdDate;

          //restore original values
          this.originalEmail = this.Email;
          this.originalFullName = this.FullName;
          this.originalPhoneNumber = this.PhoneNumber;
          this.originalAddress1 = this.Address1;
          this.originalAddress2 = this.Address2;
          this.originalBusinessName = this.BusinessName;
          this.originalCity = this.City;
          this.originalState = this.State;
          this.originalPostalCode = this.PostalCode;
          this.originalCountry = this.Country;
          this.cdr.detectChanges();
        } else {
          this.LobiboxMessage('error', 'Please try again later...', 'bx bx-error');
          console.warn('UserDetailComponent: Payload is empty', data);
        }
      },
      error: (err) => {
        if (err.message === 'Session expired' || err.status === 401) {
          console.warn('User session ended. Redirecting...');
          this.router.navigate(['/login']);
        } else {
          // Optional: Show a UI message
        }
      }
    });
  }

  editUser(): void {
    this.isEditMode = true;
  }

  cancelEdit(): void {
    this.isEditMode = false;
    this.Email = this.originalEmail;
    this.FullName = this.originalFullName;
    this.PhoneNumber = this.originalPhoneNumber;
    this.Address1 = this.originalAddress1;
    this.Address2 = this.originalAddress2;
    this.BusinessName = this.originalBusinessName;
    this.City = this.originalCity;
    this.State = this.originalState;
    this.PostalCode = this.originalPostalCode;
    this.cdr.detectChanges();
  }

  saveEdit(): void {
    if (!this.FullName) {
      if (isPlatformBrowser(this.platformId)) {
        this.LobiboxMessage('warning', 'Full Name cannot be empty', 'bx bx-error');
      }
      return;
    }

    const userDetailRequest: Userdetailrequest = {
      fullName: this.FullName,
      businessName: this.BusinessName,
      phoneNumber: this.PhoneNumber,
      address1: this.Address1,
      address2: this.Address2,
      city: this.City,
      state: this.State,
      country: this.Country,
      postCode: this.PostalCode
    }
    if(this.userIdentity === ''){
      this.LobiboxMessage('warning', 'User identity cannot be empty', 'bx bx-error');
      console.warn('UserDetailComponent: User identity is empty');
      return;
    }

    this.userDetailService.updateUsersDetail(this.userIdentity, userDetailRequest)
      .subscribe({
        next: (data) => {
          if (isPlatformBrowser(this.platformId)) {
            this.LobiboxMessage('warning', 'Data berhasil diperbaharui', 'bx bx-check-circle');
          }
          this.isEditMode = false;
          this.originalEmail = this.Email;
          this.originalFullName = this.FullName;
          this.originalBusinessName = this.BusinessName;
          this.originalPhoneNumber = this.PhoneNumber;
          this.originalAddress1 = this.Address1;
          this.originalAddress2 = this.Address2;
          this.originalBusinessName = this.BusinessName;
          this.originalCity = this.City;
        },
        error: (error) => {
          console.error('UserDetailComponent: Update user detail failed', error);
        }
      });
    this.isEditMode = false;
    this.cdr.detectChanges();
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
      console.warn('Lobibox is not defined.Ensure it is loaded correctly.');
    }
  }
}
