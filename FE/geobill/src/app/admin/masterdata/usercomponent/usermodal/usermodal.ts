import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms";
import { Userservice } from '../userservice';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-usermodal',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './usermodal.html',
  styleUrl: './usermodal.css',
})
export class Usermodal implements OnInit, OnChanges {

  @Input()
  show: boolean = false;

  @Input()
  validateEmail: boolean | null = null;

  emailValidationClass: string = 'form-control';

  @Input()
  formData: any;

  @Output()
  close = new EventEmitter<void>();

  @Output()
  formSubmit = new EventEmitter<any>();

  @ViewChild('modalContainer') modalContainer?: ElementRef;

  userForm !: FormGroup;

  constructor(
    private fb: FormBuilder,
    private el: ElementRef,
    private userService: Userservice,
    private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.userForm = this.fb.group({
      name: [this.formData ? this.formData.name || '' : '', [Validators.required, Validators.maxLength(100)]],
      email: [this.formData ? this.formData.email || '' : '', [Validators.required, Validators.maxLength(100), Validators.email]],
      password: [
        this.formData ? this.formData.password || '' : '',
        [Validators.required, Validators.maxLength(16), Validators.minLength(6)]
      ],
    });
    this.validateEmail = null;
    this.emailValidationClass = 'form-control';

    this.userForm.get('email')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(email => {
      if (!email) {
        this.validateEmail = null;
        this.emailValidationClass = 'form-control';
      } else if (this.userForm.get('email')?.valid) {
        this.doValidateEmail(email);
      } else {
        this.validateEmail = null;
        this.emailValidationClass = 'form-control';
      }
    });
    this.cdr.detectChanges();
  }

  ngOnChanges(changes: any): void {
    if (changes['formData'] && this.userForm) {
      this.userForm.reset({
        name: this.formData?.name || '',
        email: this.formData?.email || '',
        password: this.formData?.password || ''
      });
      this.validateEmail = null;
      this.emailValidationClass = 'form-control';
    }

    if (changes['show']) {
      const modalElement = this.modalContainer?.nativeElement;
      if (modalElement) {
        if (this.show) {
          modalElement.removeAttribute('aria-hidden');
          modalElement.removeAttribute('inert');
          modalElement.classList.add('show');
          modalElement.style.display = 'block';
          document.body.classList.add('modal-open');
          this.addBackdrop();
        } else {
          this.blurModalFocus(modalElement);
          setTimeout(() => {
            modalElement.classList.remove('show');
            modalElement.style.display = 'none';
            modalElement.setAttribute('aria-hidden', 'true');
            modalElement.setAttribute('inert', 'true');
            document.body.classList.remove('modal-open');
            this.removeBackdrop();
          }, 0);
        }
      }
    }
  }

  onClose() {
    this.removeBackdrop();
    this.clearForm();
    document.body.classList.remove('modal-open');
    this.close.emit();
  }

  private addBackdrop(): void {
    this.removeBackdrop();
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop fade show';
    backdrop.id = 'user-modal-backdrop';
    document.body.appendChild(backdrop);
  }

  private removeBackdrop(): void {
    const existingBackdrop = document.getElementById('user-modal-backdrop');
    if (existingBackdrop) {
      existingBackdrop.remove();
    }
  }

  private blurModalFocus(modalElement: HTMLElement): void {
    const activeElement = document.activeElement as HTMLElement;
    if (activeElement && modalElement.contains(activeElement)) {
      activeElement.blur();
      document.body.focus();
    }
  }

  onConfirm() {
    this.formSubmit.emit(this.userForm.value);
  }

  clearForm() {
    this.userForm.reset({
      name: '',
      email: '',
      password: ''
    });
    this.validateEmail = null;
    this.emailValidationClass = 'form-control';
    this.cdr.detectChanges();
  }

  validateAndConfirm() {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }
    if (this.validateEmail === false) {
      return;
    }
    this.onConfirm();
  }

  doValidateEmail(email: string) {
    this.userService.validateEmail(email).subscribe({
      next: (response) => {
        if (response.status === true) {
          console.log('Email is valid', response);
          this.validateEmail = true;
          this.emailValidationClass = 'form-control is-valid';
        } else {
          console.log('Email is invalid', response);
          this.validateEmail = false;
          this.emailValidationClass = 'form-control is-invalid';
        }
      },
      error: (e) => {
        console.log('Email is invalid: ', e);
        this.validateEmail = false;
        this.emailValidationClass = 'form-control is-invalid';
      }
    });
  }
}
