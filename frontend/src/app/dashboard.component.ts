import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  applicant: any = null;
  loading = true;
  name = 'Applicant';

  currentHour = new Date().getHours();

  activeSection = 'top';

  constructor(
    private router: Router,
    private api: ApiService,
    private auth: AuthService
  ) {}

  get greeting(): string {
    if (this.currentHour < 12) {
      return 'Good morning';
    }

    if (this.currentHour < 17) {
      return 'Good afternoon';
    }

    return 'Good evening';
  }

  get applicationStatus(): string {
    return this.applicant?.application_status ||
      this.applicant?.applicationStatus ||
      'In Progress';
  }

  get kycStatus(): string {
    return this.applicant?.kyc_status ||
      this.applicant?.kycStatus ||
      'Pending';
  }

  get accountNumber(): string | null {
    return this.applicant?.account_number ||
      this.applicant?.accountNumber ||
      null;
  }

  get applicationRejectionReason(): string {
    return this.applicant?.application_rejection_reason ||
      this.applicant?.applicationRejectionReason ||
      'No rejection reason was provided.';
  }

  get isApplicationRejected(): boolean {
    return this.applicationStatus.trim().toUpperCase() === 'REJECTED';
  }

  get kycRejectionReason(): string {
    return this.applicant?.kyc_rejection_reason ||
      this.applicant?.rejection_reason ||
      this.applicant?.kyc_remarks ||
      'No rejection reason was provided.';
  }

  ngOnInit(): void {
    this.name = this.auth.name || 'Applicant';

    const id = this.auth.id;

    if (!id) {
      this.loading = false;
      this.router.navigate(['/login']);
      return;
    }

    this.api.getApplicant(id).subscribe({
      next: (data) => {
        this.applicant = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load applicant:', error);
        this.loading = false;
      }
    });
  }

  selectSection(section: string): void {
    this.activeSection = section;
  }

  logout(): void {
    this.auth.clear();
    this.router.navigate(['/login']);
  }
}
