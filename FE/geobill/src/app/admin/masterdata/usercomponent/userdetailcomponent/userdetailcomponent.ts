/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {Menutop} from '../../../../share/menutop/menutop';
import {Footer} from '../../../../share/footer/footer';
import {Userdetailservice} from './userdetailservice';

@Component({
  selector: 'app-userdetailcomponent',
  standalone: true,
  imports: [CommonModule, RouterModule, Menutop, Footer],
  templateUrl: './userdetailcomponent.html',
  styleUrl: './userdetailcomponent.css',
})
export class Userdetailcomponent implements OnInit{
  homeLink = '/admin/dashboard';
  userEmail: string = 'User Profile';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private userDetailService: Userdetailservice) { }

  ngOnInit(): void {
    this.homeLink= this.router.getCurrentNavigation()?.extras.state?.['homeLink'] || '/admin/users';
    this.userEmail = this.router.getCurrentNavigation()?.extras.state?.['userEmail '] || 'User Profile';
    this.route.paramMap.subscribe(params => {
      const userId = params.get('uuid');
      if(userId) this.loadUserDetails(userId);
    })
  }

  loadUserDetails(userId:string) : void {

  }



}
