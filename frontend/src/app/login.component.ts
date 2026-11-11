import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  errorMessage = '';

  constructor(
    private router: Router,
    private api: ApiService,
    private auth: AuthService
  ) {}

  login() {
    if (!this.email || !this.password) {
      this.errorMessage = 'Please enter email and password.';
      return;
    }
    this.loading = true;
    this.errorMessage = '';

    this.api.login(this.email, this.password).subscribe({
      next: (res: any) => {
        this.auth.store(res.token, res.subjectId, res.name);
        this.router.navigate(['/dashboard']);
      },
      error: (err: any) => {
        this.loading = false;
        this.errorMessage = 'Invalid email or password.';
      }
    });
  }

  goHome() { this.router.navigate(['/']); }
}
