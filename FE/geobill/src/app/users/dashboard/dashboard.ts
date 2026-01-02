import { Component, OnInit } from '@angular/core';
import { Localstorageservice } from '../../utils/localstorage/localstorageservice';
import {Sidebar} from '../../share/sidebar/sidebar';
import {Menutop} from '../../share/menutop/menutop';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [
    Menutop
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class UserDashboard implements OnInit {

  constructor(private localStorage: Localstorageservice) { }

  ngOnInit() {
    // Dashboard user initialization
  }

}

