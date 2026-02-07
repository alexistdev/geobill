/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import { Component } from '@angular/core';
import {Menutop} from "../../share/menutop/menutop";
import {Localstorageservice} from '../../utils/localstorage/localstorageservice';
import {Topheader} from '../../share/topheader/topheader';

@Component({
  selector: 'app-dashboard',
  imports: [
    Menutop,
    Topheader
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard {
  constructor(private localStorage: Localstorageservice) { }

  ngOnInit() {

  }
}
