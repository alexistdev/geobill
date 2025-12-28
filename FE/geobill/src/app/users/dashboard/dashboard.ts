import { Component, OnInit } from '@angular/core';
import { Localstorageservice } from '../../utils/localstorage/localstorageservice';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class UserDashboard implements OnInit {

  constructor(private localStorage: Localstorageservice) { }

  ngOnInit() {
    // Dashboard user initialization
  }

}

