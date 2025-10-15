import { registerPlugin } from '@capacitor/core';

import type { AmapPlugin } from './definitions';

/**
 * 注册高德地图插件
 */
const Amap = registerPlugin<AmapPlugin>('Amap', {
  web: () => import('./web').then((m) => new m.AmapWeb()),
});

export * from './definitions';
export { Amap };

