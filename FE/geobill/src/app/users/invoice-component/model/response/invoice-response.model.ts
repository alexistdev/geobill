/*
 * Copyright (c) 2026.
 * Project : GeoBill
 * Author : Alexsander Hendra Wijaya
 * Github : https://github.com/alexistdev
 * Email : alexistdev@gmail.com
 */
export interface InvoiceResponseModel {
  id: string;
  hostingId: string;
  invoiceCode: string;
  detail: string;
  subTotal: number;
  total: number;
  tax: number;
  discount: number;
  startDate: Date;
  endDate: Date;
  status: number;
  cycle: number;
}
