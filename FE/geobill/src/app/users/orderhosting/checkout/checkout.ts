import { Component } from '@angular/core';
import {Footer} from "../../../share/footer/footer";
import {Header} from "../../../share/header/header";
import {Menutop} from "../../../share/menutop/menutop";

@Component({
  selector: 'app-checkout',
    imports: [
        Footer,
        Header,
        Menutop
    ],
  templateUrl: './checkout.html',
  styleUrl: './checkout.css',
})
export class Checkout {

}
