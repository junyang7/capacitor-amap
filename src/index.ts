import { registerPlugin } from '@capacitor/core';

import type { AmapLocationPlugin } from './definitions';

/**
 * 注册高德定位插件
 */
const AmapLocation = registerPlugin<AmapLocationPlugin>('AmapLocation', {
  web: () => import('./web').then((m) => new m.AmapLocationWeb()),
});

export * from './definitions';
export { AmapLocation };

