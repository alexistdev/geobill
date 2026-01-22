/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

export interface Productmodel {
  id: string;
  name: string;
  productTypeDTO: {
    id: string;
    name: string;
  }
  price: number;
  cycle: number;
  capacity: string | null,
  bandwith: string | null,
  addon_domain: string | null,
  database_account: string | null,
  ftp_account: string | null,
  info1: string | null,
  info2: string | null,
  info3: string | null,
  info4: string | null,
  info5: string | null
}
