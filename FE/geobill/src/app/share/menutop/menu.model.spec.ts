import { Menu } from './menu.model';

describe('Menu', () => {
  it('should accept an object matching the interface', () => {
    const menu: Menu = {
      id: '1',
      name: 'Dashboard',
      urlink: '/dashboard',
      classlink: 'nav-link',
      sortOrder: '1',
      icon: 'fa fa-home',
      typeMenu: 1,
      code: 'DASHBOARD',
      parentId: null,
    };

    expect(menu.name).toBe('Dashboard');
  });
});
