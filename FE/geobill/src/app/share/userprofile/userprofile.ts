import { Component } from '@angular/core';
import {LogoutService} from '../../utils/auth/logout.service';

@Component({
  selector: 'app-userprofile',
  standalone: true,
  imports: [],
  templateUrl: './userprofile.html',
  styleUrl: './userprofile.css',
})
export class Userprofile {


  constructor(private logoutService:LogoutService) {
  }

  logout(){
    this.logoutService.logout();
  }
}
