import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'kcb.token';
  private readonly ID_KEY = 'kcb.id';
  private readonly NAME_KEY = 'kcb.name';

  store(token: string, id: string, name: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.ID_KEY, id);
    localStorage.setItem(this.NAME_KEY, name);
  }

  get token(): string | null { return localStorage.getItem(this.TOKEN_KEY); }
  get id(): string | null { return localStorage.getItem(this.ID_KEY); }
  get name(): string | null { return localStorage.getItem(this.NAME_KEY); }

  isLoggedIn(): boolean { return !!this.token; }

  clear(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.ID_KEY);
    localStorage.removeItem(this.NAME_KEY);
  }
}
