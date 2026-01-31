/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import {ChangeDetectorRef, Component, ElementRef, inject, NgZone, OnInit, PLATFORM_ID} from '@angular/core';
import {DecimalPipe} from '@angular/common';
import {Menutop} from '../../../share/menutop/menutop';
import {Pagination} from '../../../share/pagination/pagination';
import {Usermodel} from './usermodel.model';
import {Payload} from '../../../share/response/payload';
import {Subject} from 'rxjs';
import {Userservice} from './userservice';
import {Router} from '@angular/router';

@Component({
  selector: 'app-usercomponent',
  imports: [
    Menutop,
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
  }


  loadData(page: number, size: number = 10): void {
    this.pageNumber = page;
    this.pageSize = size;
    this.keyword = this.searchQuery;
    const sortBy = 'createdDate';
    const direction = 'desc';
    const isFiltering = this.keyword !== "";
  }

}
