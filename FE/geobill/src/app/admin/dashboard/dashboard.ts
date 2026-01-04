import { Component } from '@angular/core';
import {Menutop} from "../../share/menutop/menutop";
import {Localstorageservice} from '../../utils/localstorage/localstorageservice';

@Component({
  selector: 'app-dashboard',
    imports: [
        Menutop
    ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard {
  constructor(private localStorage: Localstorageservice) { }

  ngOnInit() {

  }
}
