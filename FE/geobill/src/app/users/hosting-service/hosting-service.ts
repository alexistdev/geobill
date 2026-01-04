import { Component } from '@angular/core';
import {Menutop} from "../../share/menutop/menutop";
import {Footer} from '../../share/footer/footer';
import {Header} from '../../share/header/header';

@Component({
  selector: 'app-hosting-service',
    imports: [
        Menutop,
      Footer,
      Header
    ],
  templateUrl: './hosting-service.html',
  styleUrl: './hosting-service.css'
})
export class HostingService {

}
