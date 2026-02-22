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
import {Usermodal} from './usermodal/usermodal';
import {Userregisterrequest} from './request/userregisterrequest.model';
declare var Lobibox: any;

@Component({
  selector: 'app-usercomponent',
  standalone: true,
  imports: [
    Menutop,
    DatePipe,
    RouterModule,
    Footer,
    Topheader,
    Usermodal
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
  private searchSubject = new Subject<string>();
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

  openModal() {
    this.showModal = true;
  }

  closeModal() {
    this.el.nativeElement.blur();
    this.showModal = false;
    this.cdr.detectChanges();
  }

  goToDetail(user: Usermodel) {
    this.router.navigate(['/admin/users', user.id]);
  }

  doSaveData(formValue:any){
    const request : Userregisterrequest = {
      fullName: formValue.name,
      email: formValue.email,
      password: formValue.password
    }
    this.userService.saveUser(request).subscribe({
      next: () => {
        if(isPlatformBrowser(this.platformId)){
          this.LobiboxMessage('success', 'Data berhasil disimpan','bx bx-check-circle');
        }
        this.closeModal();
        this.loadData(this.pageNumber, this.pageSize);
      },
      error: (err) => {
        let errorMessage = 'An unexpected error occurred.';
        try {
          console.error(err);
          errorMessage = err.error?.messages?.[0] || errorMessage;
        } catch (e){
          console.error('Error while processing error:', e);
        }
        this.LobiboxMessage('error', errorMessage,'bx bx-x-circle');
        this.ngZone.run(() => {
          this.closeModal();
        });
      }
    })
  }

  LobiboxMessage(type: string, msg: string, icon: string):void {
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
