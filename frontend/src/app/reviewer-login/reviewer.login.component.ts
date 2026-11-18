
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface StaffLoginResponse {
  token: string;
  subjectId: string;
  role: string;
  name: string;
}

@Component({
  selector: 'app-reviewer-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './reviewer.login.component.html',
  styleUrl: './reviewer.login.component.css',
})
export class ReviewerLoginComponent {
  staffId = '';
  password = '';
  loading = false;
  errorMessage = '';

  private readonly apiUrl = 'http://localhost:8080/api/auth/staff-login';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(): void {
    if (!this.staffId.trim() || !this.password.trim()) {
      this.errorMessage = 'Please enter your Staff ID and password.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.http.post<StaffLoginResponse>(this.apiUrl, {
      staffId: this.staffId.trim(),
      password: this.password
    }).subscribe({
      next: (res) => {
        if (res.role !== 'REVIEWER') {
          this.loading = false;
          this.errorMessage = 'This account is not authorized for the reviewer portal.';
          return;
        }

        localStorage.setItem('kcb.reviewer.token', res.token);
        localStorage.setItem('kcb.reviewer.id', res.subjectId);
        localStorage.setItem('kcb.reviewer.name', res.name);
        localStorage.setItem('kcb.reviewer.role', res.role);

        this.router.navigate(['/reviewer']);
      },

      error: (err) => {
        console.error('Reviewer login failed:', err);
        this.loading = false;
        this.errorMessage =
          err.error?.message || 'Invalid Staff ID or password.';
      }
    });
  }

  goHome(): void {
    this.router.navigate(['/']);
  }
}
