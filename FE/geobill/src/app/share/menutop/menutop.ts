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
    return menuItems
      .filter(item => item.parentId === parentId)
      .map(item => {
        const newItem = { ...item };
        const children = this.buildMenuTree(menuItems, newItem.id);

        if (children.length > 0) {
          newItem.children = children;
        }
        return newItem;
      })
      .sort((a, b) => {
        const orderA = parseInt(a.sortOrder) || 0;
        const orderB = parseInt(b.sortOrder) || 0;
        return orderA - orderB;
      });
  }
}
