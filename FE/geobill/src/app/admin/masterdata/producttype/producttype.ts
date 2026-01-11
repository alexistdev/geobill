import { Component } from '@angular/core';
import {Menutop} from '../../../share/menutop/menutop';
import {Searchmodal} from '../../../share/searchmodal/searchmodal';

@Component({
  selector: 'app-producttype',
  imports: [
    Menutop,
    Searchmodal
  ],
  templateUrl: './producttype.html',
  styleUrl: './producttype.css'
})
export class Producttype {

}
