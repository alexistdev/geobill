import { Component } from '@angular/core';
import {Footer} from "../../share/footer/footer";
import {Header} from "../../share/header/header";
import {Menutop} from "../../share/menutop/menutop";

@Component({
  selector: 'app-invoice-component',
    imports: [
        Footer,
        Header,
        Menutop
    ],
  templateUrl: './invoice-component.html',
  styleUrl: './invoice-component.css',
})
export class InvoiceComponent {

}
