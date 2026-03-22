import { Component } from '@angular/core';
import {Notification} from '../notification/notification';
import {Userprofile} from '../userprofile/userprofile';

@Component({
  selector: 'app-header',
  imports: [
    Notification,
    Userprofile
  ],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header {

}
