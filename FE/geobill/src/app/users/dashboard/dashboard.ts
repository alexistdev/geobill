import { Component, OnInit } from '@angular/core';
import { Localstorageservice } from '../../utils/localstorage/localstorageservice';
import {Menutop} from '../../share/menutop/menutop';
import {Footer} from '../../share/footer/footer';
import {Header} from '../../share/header/header';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [
    Menutop,
    Footer,
    Header
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class UserDashboard implements OnInit {

  constructor(private localStorage: Localstorageservice) { }

  ngOnInit() {

  }

}

