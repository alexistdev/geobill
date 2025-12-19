import {ChangeDetectorRef, Component, OnInit, NgZone} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { Router } from '@angular/router';
import {Loginservice} from './loginservice';
import {Localstorageservice} from '../utils/localstorage/localstorageservice';
import {NgIf} from '@angular/common';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login implements OnInit {

  loginForm!: FormGroup;

  public loginError:boolean = false;
  isAdmin:string = '';

  constructor(
    private router: Router,
    private formBuilder: FormBuilder,
    private loginService: Loginservice,
    private localStorage: Localstorageservice,
    private cdRef: ChangeDetectorRef,
    private ngZone: NgZone) {
  }

  ngOnInit() {
    this.loginForm = this.formBuilder.group({
      emailUsername: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  doLogin(){
    if(this.loginForm.valid){
      // Optional: this.loginForm.disable();
      this.loginService.AuthLogin(
        this.loginForm.controls['emailUsername'].value,
        this.loginForm.controls['password'].value
      ).subscribe({
        next: (res: any) => {
          this.ngZone.run(() => {
            if(res && res.success){
              this.loginError = false;
              this.router.navigate(['/user']);
            } else {
              console.log("posisi:2 - Login Failed");
              this.loginError = true;
            }
            this.cdRef.markForCheck();
          });
        },
        error: (err) => {
          this.ngZone.run(() => {
            console.log("posisi:3 - Error");
            this.loginError = true;
            this.cdRef.markForCheck();
          });
        }
      });
    } else {
      this.doReset();
      this.loginError = true;
    }
  }

  doReset(){
    this.loginForm.reset();
    this.loginError=false;
    this.loginForm.enable();
  }
}
