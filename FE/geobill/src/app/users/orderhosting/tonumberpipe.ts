/*
 * Copyright (c) 2026.
 * Project : GeoBill
 * Author : Alexsander Hendra Wijaya
 * Github : https://github.com/alexistdev
 * Email : alexistdev@gmail.com
 */

import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'toNumber'
})
export class ToNumberPipe implements PipeTransform {
  transform(value: string): number {
    return Number(value);
  }
}
