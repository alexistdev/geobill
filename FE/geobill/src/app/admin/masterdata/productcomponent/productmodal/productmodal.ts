import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Producttypeservice} from '../../producttype/producttypeservice';
import {Producttypemodel} from '../../producttype/producttypemodel.model';

@Component({
  selector: 'app-productmodal',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './productmodal.html',
  styleUrl: './productmodal.css',
})
export class Productmodal implements OnInit, OnChanges {
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

  productForm !: FormGroup;
  protected productTypes: Producttypemodel[] = [];

  constructor(private fb: FormBuilder,
              private el: ElementRef,
              private producttypeservice: Producttypeservice) {
  }

  ngOnInit(): void {
    this.loadProductType();
    this.productForm = this.fb.group({
      id: this.formData?.id || null,
      name: [this.formData.name || '', [Validators.required, Validators.maxLength(100)]],
      productTypeId: [this.formData?.productTypeId?.id || '', [Validators.required, Validators.maxLength(100)]],
      price: [this.formData.price || 0, [Validators.required, Validators.min(0)]],
      cycle: [this.formData.cycle || 0, [Validators.required, Validators.min(1), Validators.max(12), Validators.pattern('^[0-9]*$')]],
      capacity: [this.formData.database_account || '', [Validators.maxLength(100)]],
      bandwith: [this.formData.bandwith || '', [Validators.maxLength(100)]],
      addon_domain: [this.formData.addon_domain || '', [Validators.maxLength(100)]],
      database_account: [this.formData.database_account || '', [Validators.maxLength(100)]],
      ftp_account: [this.formData.ftp_account || '', [Validators.maxLength(100)]],
      info1: [this.formData.info1 || '', [Validators.maxLength(100)]],
      info2: [this.formData.info2 || '', [Validators.maxLength(100)]],
      info3: [this.formData.info3 || '', [Validators.maxLength(100)]],
      info4: [this.formData.info4 || '', [Validators.maxLength(100)]],
      info5: [this.formData.info5 || '', [Validators.maxLength(100)]],
    });
  }

  loadProductType():void {
    this.producttypeservice.getProductType(0,0,"id","asc").
    subscribe({
      next:(data) => this.productTypes = data.payload.content,
      error:(err) => console.log(err)
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['formData'] && this.productForm) {
      this.productForm.patchValue({
        id: this.formData?.id || null,
        name: this.formData?.name || '',
        productTypeId: this.formData?.productTypeId?.id || null,
        price: this.formData?.price || '',
        cycle: this.formData?.cycle || '',
        capacity: this.formData?.capacity || '',
        bandwith: this.formData?.bandwith || '',
        addon_domain: this.formData?.addon_domain || '',
        database_account: this.formData?.database_account || '',
        ftp_account: this.formData?.ftp_account || '',
        info1: this.formData?.info1 || '',
        info2: this.formData?.info2 || '',
        info3: this.formData?.info3 || '',
        info4: this.formData?.info4 || '',
        info5: this.formData?.info5 || '',
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
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }
    this.onConfirm();
  }

  onConfirm() {
    this.formSubmit.emit(this.productForm.value);
    this.clearForm();
  }

  clearForm() {
    this.productForm.reset({
      id: '',
      name: '',
      productTypeId: '',
      price: '',
      cycle: '',
      capacity: '',
      bandwith: '',
      addon_domain: '',
      database_account: '',
      ftp_account: '',
      info1: '',
      info2: '',
      info3: '',
      info4: '',
      info5: '',
    });
  }

  deleteConfirm(): void {
    this.confirmDelete.emit();
  }
}
