import { Component } from '@angular/core';
import {Footer} from '../../share/footer/footer';
import {Menutop} from '../../share/menutop/menutop';
import {Topheader} from '../../share/topheader/topheader';

@Component({
  selector: 'app-orderservice',
  imports: [
    Footer,
    Menutop,
    Topheader
  ],
  templateUrl: './orderservice.html',
  styleUrl: './orderservice.css',
})
export class Orderservice {

}
