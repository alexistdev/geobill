/*
 * Copyright (c) 2026.
 * Project : GeoBill
 * Author : Alexsander Hendra Wijaya
 * Github : https://github.com/alexistdev
 * Email : alexistdev@gmail.com
 */
import {InvoiceModel} from './model/invoice.model';
import {CustomerModel} from './model/customer.model';

export interface Orderhostingresponse {
  id: string;
  userId: string;
  productId: string;
  domainName: string;
  price: number;
  cycle: number;
  invoiceDTO: InvoiceModel;
  customerDTO: CustomerModel;
}

