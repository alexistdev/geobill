/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  ElementRef,
  ViewChild
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-producttypemodal',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './producttypemodal.html',
  styleUrl: './producttypemodal.css',
})
export class Producttypemodal implements OnInit, OnChanges {
  @Input()
  show: boolean = false;

  @Input()
  modalType: 'form' | 'confirm' | undefined;

  @Input()
  validateEmail: boolean | null = null;

  @Input()
  isEditMode: boolean = false;

  @Input()
  originalEmail: string = '';

  @Input()
  formData: any;

  @Input()
  confirmationText: string = '';

  @Output()
  close = new EventEmitter<void>();

  @Output()
  emailChanged = new EventEmitter<string>();

  @Output()
  formSubmit = new EventEmitter<any>();

  @Output()
  confirmDelete = new EventEmitter<void>();

  @ViewChild('modalContainer') modalContainer?: ElementRef;

  userForm !: FormGroup;

  constructor(private fb: FormBuilder, private el: ElementRef) {
  }

  ngOnInit(): void {
    this.userForm = this.fb.group({
      id: this.formData?.id || null,
      name: [this.formData.name || '', [Validators.required, Validators.maxLength(100)]]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['formData'] && this.userForm) {
      this.userForm.reset({
        id: this.formData?.id || null,
        name: this.formData?.name || ''
      });
    }

    if (changes['show']) {
      const modalElement = this.modalContainer?.nativeElement;
      if (modalElement) {
        if (this.show) {
          // Show modal
          modalElement.removeAttribute('aria-hidden');
          modalElement.removeAttribute('inert');
          modalElement.classList.add('show');
          modalElement.style.display = 'block';
          document.body.classList.add('modal-open');

          // Add backdrop
          this.addBackdrop();
        } else {
          // Hide modal - blur focus first to prevent aria-hidden warning
          this.blurModalFocus(modalElement);

          // Use setTimeout to ensure focus is moved before hiding
          setTimeout(() => {
            modalElement.classList.remove('show');
            modalElement.style.display = 'none';
            modalElement.setAttribute('aria-hidden', 'true');
            modalElement.setAttribute('inert', 'true');
            document.body.classList.remove('modal-open');

            // Remove backdrop
            this.removeBackdrop();
          }, 0);
        }
      }
    }
  }

  private addBackdrop(): void {
    this.removeBackdrop();

    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop fade show';
    backdrop.id = 'producttype-modal-backdrop';
    document.body.appendChild(backdrop);
  }

  private removeBackdrop(): void {
    const existingBackdrop = document.getElementById('producttype-modal-backdrop');
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

  onClose() {
    this.removeBackdrop();
    document.body.classList.remove('modal-open');
    this.close.emit();
  }

  validateAndConfirm() {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }
    this.onConfirm();
  }

  onConfirm() {
    this.formSubmit.emit(this.userForm.value);
    this.clearForm();
  }

  clearForm() {
    this.userForm.reset({
      id: '',
      name: ''
    });
  }

  deleteConfirm(): void {
    this.confirmDelete.emit();
  }
}
