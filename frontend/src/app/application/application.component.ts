
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../api.service';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-application',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './application.component.html',
  styleUrl: './application.component.css'
})
export class ApplicationComponent implements OnInit {
  applicant: any = null;
  loading = true;
  errorMessage = '';

  constructor(
    private api: ApiService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.auth.id;

    if (!id || !this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.api.getApplicant(id).subscribe({
      next: (data: any) => {
        this.applicant = data;
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Unable to load application:', error);
        this.errorMessage = 'Unable to load your application. Please try again.';
        this.loading = false;
      }
    });
  }

  getStatus(status: string): string {
    return (status || 'PENDING_REVIEW').replace(/_/g, ' ');
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
