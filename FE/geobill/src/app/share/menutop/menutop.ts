import {Component, OnInit} from '@angular/core';
import {NgClass} from '@angular/common';
import {Localstorageservice} from '../../utils/localstorage/localstorageservice';
import {Menu} from './menu.model';


@Component({
  selector: 'app-menutop',
  standalone: true,
  imports: [NgClass],
  templateUrl: './menutop.html',
  styleUrl: './menutop.css'
})
export class Menutop implements OnInit{
  public menus: Menu[] = [];

  constructor(private localStorage: Localstorageservice) { }

  ngOnInit() {
    const storedMenus = this.localStorage.getItemAsObject<any[]>('menus');
    if(storedMenus) {
      this.menus = this.buildMenuTree(storedMenus);
    }
  }

  buildMenuTree(menuItems: Menu[], parentId: string | null = null): Menu[] {
    const result: Menu[] = [];

    for(const item of menuItems) {
      if(item.parentId === parentId) {
        const children = this.buildMenuTree(menuItems, item.id);
        if(children.length > 0) {
          item.children = children;
        }
        result.push(item);
      }
    }

    return result;
  }
}
