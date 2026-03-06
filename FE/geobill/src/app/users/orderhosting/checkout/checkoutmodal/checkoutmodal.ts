import { ChangeDetectorRef, Component, ElementRef, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from "@angular/forms";

@Component({
  selector: 'app-checkoutmodal',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './checkoutmodal.html',
  styleUrl: './checkoutmodal.css',
})
export class Checkoutmodal implements OnInit, OnChanges {
  @Input()
  show: boolean = false;

  @Output()
  close = new EventEmitter<void>();

  @Output()
  confirmSubmit = new EventEmitter<void>();

  @ViewChild('modalContainer') modalContainer?: ElementRef;

  constructor(private cdr: ChangeDetectorRef) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
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
          modalElement.classList.remove('show');
          modalElement.style.display = 'none';
          modalElement.setAttribute('aria-hidden', 'true');
          modalElement.setAttribute('inert', 'true');
          document.body.classList.remove('modal-open');
          this.removeBackdrop();
        }
      }
    }
  }

  private addBackdrop(): void {
    this.removeBackdrop();
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop fade show';
    backdrop.id = 'checkout-modal-backdrop';
    document.body.appendChild(backdrop);
  }

  private removeBackdrop(): void {
    const existingBackdrop = document.getElementById('checkout-modal-backdrop');
    if (existingBackdrop) {
      existingBackdrop.remove();
    }
  }

  onClose() {
    this.removeBackdrop();
    document.body.classList.remove('modal-open');
    this.close.emit();
  }

  onSubmit() {
      this.confirmSubmit.emit();
  }
}
