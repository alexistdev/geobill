/*
 * Copyright (c) 2026.
 * Project : GeoBill
 * Author : Alexsander Hendra Wijaya
 * Github : https://github.com/alexistdev
 * Email : alexistdev@gmail.com
 */

import {Customer} from './customer.model';

export interface UserDetailModel {
  id: string;
  fullName: string;
  email: string;
  createdDate?: string | null;
  modifiedDate?: string | null;
  customer: Customer;
}
