import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class Localstorageservice {

  constructor() { }

  setItem(key: string, val:string):void {
    sessionStorage.setItem(key,this.encode(val));
  }

  getItem(key:string):string {
    return <string>sessionStorage.getItem(key);
  }

  clearItem():void {
    sessionStorage.clear();
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
