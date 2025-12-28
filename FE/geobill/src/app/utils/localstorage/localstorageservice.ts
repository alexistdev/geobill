import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class Localstorageservice {
  private platformId = inject(PLATFORM_ID);

  constructor() { }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  setItem(key: string, val:string):void {
    if (this.isBrowser()) {
      sessionStorage.setItem(key,this.encode(val));
    }
  }

  getItem(key:string):string {
    if (!this.isBrowser()) {
      return '';
    }
    const item = sessionStorage.getItem(key);
    if (item) {
      try {
        return this.decode(item);
      } catch (e) {
        return item;
      }
    }
    return '';
  }

  getItemAsObject<T>(key: string): T | null {
    const item = this.getItem(key);
    if (item) {
      try {
        return JSON.parse(item) as T;
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  clearItem():void {
    if (this.isBrowser()) {
      sessionStorage.clear();
    }
  }

  private encode(value: string): string {
    return btoa(
      encodeURIComponent(value).replace(/%([0-9A-F]{2})/g, (_, p1) =>
        String.fromCharCode(parseInt(p1, 16))
      )
    );
  }

  public decode(value: string): string {
    return decodeURIComponent(
      Array.prototype.map.call(atob(value), (c: string) =>
        `%${c.charCodeAt(0).toString(16).padStart(2, '0')}`
      ).join('')
    );
  }
}
