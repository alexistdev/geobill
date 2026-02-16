/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import { Component, OnInit, PLATFORM_ID, Inject, ChangeDetectorRef, signal, WritableSignal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Menutop } from '../../../../share/menutop/menutop';
import { Footer } from '../../../../share/footer/footer';
import { Userdetailservice } from './userdetailservice';
import { Topheader } from '../../../../share/topheader/topheader';

import { UserDetailModel } from './userdetailmodel.model';
import { Customer } from './customer.model';
import { Payload } from '../../../../share/response/payload';

@Component({
  selector: 'app-userdetailcomponent',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, Menutop, Footer, Topheader],
  templateUrl: './userdetailcomponent.html',
  styleUrl: './userdetailcomponent.css',
})
export class Userdetailcomponent implements OnInit {
  homeLink = '/admin/dashboard';
  userEmail: string = 'User Profile';
  userDetail: WritableSignal<UserDetailModel | undefined> = signal(undefined);
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
        // console.log('UserDetailComponent: OnInit - UUID from route:', userId);
        if (userId) {
          this.loadUserDetails(userId);
        } else {
          // console.warn('UserDetailComponent: No UUID found in route paramMap');
        }
      })
    }
  }

  loadUserDetails(userId: string): void {
    // console.log('UserDetailComponent: Loading user details for ID:', userId);
    const request$ = this.userDetailService.getUsersDetail(userId);
    request$.subscribe({
      next: (data) => {
        // console.log('UserDetailComponent: API Response:', data);
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

          console.log(this.Email);
          console.log(this.FullName);
          console.log(this.PhoneNumber);
          console.log(this.Address1);
          console.log(this.Address2);
          console.log(this.City);
          console.log(this.State);
          console.log(this.PostalCode);
          console.log(this.Country);
          this.cdr.detectChanges();
        } else {
          // console.warn('UserDetailComponent: Payload is empty', data);
        }
      },
      error: (err) => {
        // console.error('UserDetailComponent: API Error:', err);
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

    //restore the original values
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
    console.log('UserDetailComponent: Save edit');
    this.isEditMode = false;

  }



}
