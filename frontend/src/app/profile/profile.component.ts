
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../api.service';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
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
        console.error('Profile loading error:', error);
        this.errorMessage = 'Unable to load your profile. Please try again.';
        this.loading = false;
      }
    });
  }
}
