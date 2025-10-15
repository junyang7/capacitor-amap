import { WebPlugin } from '@capacitor/core';

import type { 
  AmapLocationPlugin, 
  LocationOptions,
  PermissionStatus,
  Position,
  CallbackID 
} from './definitions';

/**
 * Web平台实现（占位符）
 * 在Web环境下，建议使用浏览器的Geolocation API
 */
export class AmapLocationWeb extends WebPlugin implements AmapLocationPlugin {
  
  async checkPermissions(): Promise<PermissionStatus> {
    throw this.unimplemented('Not implemented on web.');
  }

  async requestPermissions(): Promise<PermissionStatus> {
    throw this.unimplemented('Not implemented on web.');
  }

  async getCurrentPosition(_options: LocationOptions): Promise<Position> {
    throw this.unimplemented('Not implemented on web.');
  }

  async watchPosition(_options: LocationOptions): Promise<CallbackID> {
    throw this.unimplemented('Not implemented on web.');
  }

  async clearWatch(_options: { id: string }): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }
}

