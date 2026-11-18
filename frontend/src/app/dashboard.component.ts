
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
  showAccountNumber = false;

  constructor(
    private router: Router,
    private api: ApiService,
    public auth: AuthService
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
    return this.applicant?.status || 'Pending';
  }

  get kycStatus(): string {
    return this.applicant?.kyc_status || 'Pending';
  }

  get accountNumber(): string {
    return this.applicant?.account_number ||
      this.applicant?.accountNumber ||
      '';
  }

  get applicationRejectionReason(): string {
    return this.applicant?.rejection_reason ||
      this.applicant?.rejectionReason ||
      '';
  }

  get isApplicationRejected(): boolean {
    const status = this.applicationStatus.toLowerCase();

    return status === 'rejected' || status === 'declined';
  }

  get kycRejectionReason(): string {
    return this.applicant?.kyc_rejection_reason ||
      this.applicant?.kycRejectionReason ||
      '';
  }

  get isAccepted(): boolean {
    const status = this.applicationStatus.toLowerCase();

    return status === 'approved' || status === 'accepted';
  }

  get isRejected(): boolean {
    const status = this.applicationStatus.toLowerCase();

    return status === 'rejected' || status === 'declined';
  }

  get statusDescription(): string {
    const status = this.applicationStatus.toLowerCase();

    if (status === 'approved' || status === 'accepted') {
      return 'Your application has been accepted.';
    }

    if (status === 'rejected' || status === 'declined') {
      return 'Your application was not approved.';
    }

    if (status === 'pending') {
      return 'Your application is being reviewed.';
    }

    return 'Your application status will be updated here.';
  }

  ngOnInit(): void {

    if (!this.auth.isLoggedIn() || !this.auth.id) {
      this.router.navigate(['/login']);
      return;
    }

    this.name = this.auth.name || 'Applicant';

    const id = this.auth.id;

    this.api.getApplicant(id).subscribe({

      next: (res: any) => {
        this.applicant = res;

        if (res?.full_name) {
          this.name = res.full_name;
        } else if (res?.fullName) {
          this.name = res.fullName;
        }

        this.loading = false;
      },

      error: (err: any) => {
        console.error('Unable to load applicant details:', err);
        this.loading = false;
      }

    });
  }

  selectSection(section: string): void {
    this.activeSection = section;
  }

  toggleAccountNumber(): void {
    this.showAccountNumber = !this.showAccountNumber;
  }

  scrollToSection(sectionId: string): void {
    const section = document.getElementById(sectionId);

    if (section) {
      section.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      });
    }
  }

  viewNotifications(): void {
    this.router.navigate(['/dashboard']);
  }

  logout(): void {
    this.auth.clear();
    this.router.navigate(['/login']);
  }
}
