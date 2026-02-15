/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import { Component, OnInit, PLATFORM_ID, Inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Menutop } from '../../../../share/menutop/menutop';
import { Footer } from '../../../../share/footer/footer';
import { Userdetailservice } from './userdetailservice';
import { Topheader } from '../../../../share/topheader/topheader';

import { UserDetailModel } from './userdetailmodel.model';

@Component({
  selector: 'app-userdetailcomponent',
  standalone: true,
  imports: [CommonModule, RouterModule, Menutop, Footer, Topheader],
  templateUrl: './userdetailcomponent.html',
  styleUrl: './userdetailcomponent.css',
})
export class Userdetailcomponent implements OnInit {
  homeLink = '/admin/dashboard';
  userEmail: string = 'User Profile';
  userDetail: UserDetailModel | undefined;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private userDetailService: Userdetailservice,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  ngOnInit(): void {
    this.homeLink = this.router.getCurrentNavigation()?.extras.state?.['homeLink'] || '/admin/users';
    this.userEmail = this.router.getCurrentNavigation()?.extras.state?.['userEmail '] || 'User Profile';

    if (isPlatformBrowser(this.platformId)) {
      this.route.paramMap.subscribe(params => {
        const userId = params.get('uuid');
        if (userId) this.loadUserDetails(userId);
      })
    }
  }

  loadUserDetails(userId: string): void {
    const request$ = this.userDetailService.getUsersDetail(userId);
    request$.subscribe({
      next: (data) => {
        if (data.payload) {
          const payload: any = data.payload;
          this.userDetail = payload.content && Array.isArray(payload.content) ? payload.content[0] : payload;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        if (err.message === 'Session expired') {
          console.warn('User session ended. Redirecting...');
          this.router.navigate(['/login']);
        } else {
        console.error('Error loading user details:', err);
        }
      }
    });
  }



}
