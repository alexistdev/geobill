/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import {ChangeDetectorRef, Component, ElementRef, inject, NgZone, OnInit, PLATFORM_ID} from '@angular/core';
import {DatePipe, isPlatformBrowser} from '@angular/common';
import {Menutop} from '../../../share/menutop/menutop';
import {Usermodel} from './usermodel.model';
import {Payload} from '../../../share/response/payload';
import {debounceTime, distinctUntilChanged, Subject} from 'rxjs';
import {Userservice} from './userservice';
import {Router, RouterModule} from '@angular/router';
import {Apiresponse} from '../../../share/response/apiresponse';
import {Footer} from '../../../share/footer/footer';
import {Topheader} from '../../../share/topheader/topheader';

@Component({
  selector: 'app-usercomponent',
  standalone: true,
  imports: [
    Menutop,
    DatePipe,
    RouterModule,
    Footer,
    Topheader
  ],
  templateUrl: './usercomponent.html',
  styleUrl: './usercomponent.css',
})
export class Usercomponent implements OnInit {
  users: Usermodel[] = [];
  payload?: Payload<Usermodel>;
  totalData: number = 0;
  pageNumber: number = 0;
  totalPages: number = 0;
  pageSize: number = 10;
  keyword: string = "";
  searchQuery: string = '';

  public showModal = false;
  public currentModalType: 'form' | 'confirm' = 'confirm';
  public currentFormData: any = {};
  public currentConfirmationText = '';
  private searchSubject = new Subject<string>();
  currentEditMode: boolean = false;
  selectedUserId: string | undefined = '';
  private platformId = inject(PLATFORM_ID);
  protected readonly Number = Number;

  constructor(
    private userService: Userservice,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private el: ElementRef,
    private ngZone: NgZone
  ) {
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadData(this.pageNumber, this.pageSize);
    }
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe((searchTerm: string) => {
      this.searchQuery = searchTerm.toLowerCase();
      this.loadData(this.pageNumber, this.pageSize);
    });
  }


  loadData(page: number, size: number = 10): void {
    this.pageNumber = page;
    this.pageSize = size;
    this.keyword = this.searchQuery;
    const sortBy = 'createdDate';
    const direction = 'desc';
    const isFiltering = this.keyword !== "";

    const request$ = isFiltering ?
      this.userService.getUsersByFilter(this.keyword, this.pageNumber, this.pageSize, sortBy, direction) :
      this.userService.getUsers(this.pageNumber, this.pageSize, sortBy, direction);

    request$.subscribe({
      next:(data) => this.updateUserPageData(data),
      error:(err) => {
        if (err.message === 'Session expired') {
          console.warn('User session ended. Redirecting...');
          this.router.navigate(['/login']);
        }  else {
          console.error(err);
        }
      }
    })
  }

  private updateUserPageData(data: Apiresponse<Usermodel>) {
    this.payload = data.payload;
    this.totalData = this.payload?.totalElements ?? 0;
    this.pageNumber = this.payload.pageable.pageNumber;
    this.totalPages = this.payload.totalPages;
    this.pageSize = this.payload.pageable.pageSize;

    const newItems = this.payload.content.map(usermodel => {
      return {
        ...usermodel,
      };
    });
    this.users = [...newItems];
    this.cdr.markForCheck();
    this.cdr.detectChanges();
  }

  goToDetail(user: Usermodel) {
    this.router.navigate(['/admin/users', user.id]);
  }
}
