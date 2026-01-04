import { Component, OnInit } from '@angular/core';
import { Localstorageservice } from '../../utils/localstorage/localstorageservice';

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

