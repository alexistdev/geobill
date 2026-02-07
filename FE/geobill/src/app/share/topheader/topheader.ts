import { Component } from '@angular/core';
import {Notification} from "../notification/notification";
import {Userprofile} from '../userprofile/userprofile';

@Component({
  selector: 'app-topheader',
  imports: [
    Userprofile,
    Notification
  ],
  standalone: true,
  templateUrl: './topheader.html',
  styleUrl: './topheader.css',
})
export class Topheader {

}
