import { Component, OnInit } from '@angular/core';
import { Localstorageservice } from '../../utils/localstorage/localstorageservice';
import {Sidebar} from '../../share/sidebar/sidebar';

@Component({
  selector: 'app-staff-dashboard',
  standalone: true,
  imports: [],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class StaffDashboard implements OnInit {

  constructor(private localStorage: Localstorageservice) { }

  ngOnInit() {
    // Dashboard staff initialization
  }

}

