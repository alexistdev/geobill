import { Component, OnInit } from '@angular/core';
import { Localstorageservice } from '../../utils/localstorage/localstorageservice';
import {Menutop} from '../../share/menutop/menutop';
import {Footer} from '../../share/footer/footer';
import {Header} from '../../share/header/header';
import {Topheader} from '../../share/topheader/topheader';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [
    Menutop,
    Footer,
    Topheader
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class UserDashboard implements OnInit {

  constructor(private localStorage: Localstorageservice) { }

  ngOnInit() {

  }

}

