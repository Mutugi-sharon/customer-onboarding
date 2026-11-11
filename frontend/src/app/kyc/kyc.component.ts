
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../api.service';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-kyc',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './kyc.component.html',
  styleUrl: './kyc.component.css'
})
export class KycComponent implements OnInit {
  applicant: any = null;
  loading = true;
  errorMessage = '';

  documents = [
    { key: 'national_id_link', name: 'National ID', description: 'Identity verification' },
    { key: 'kra_link', name: 'KRA Certificate', description: 'Tax identification' },
    { key: 'photo_link', name: 'Passport Photo', description: 'Applicant photograph' },
    { key: 'address_proof_link', name: 'Proof of Address', description: 'Residential verification' },
    { key: 'income_link', name: 'Proof of Income', description: 'Income verification' },
    { key: 'employment_letter_link', name: 'Employment Letter', description: 'Employment verification' }
  ];

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

    this.loadKycDetails(id);
  }

  loadKycDetails(id: string): void {
    this.api.getApplicant(id).subscribe({
      next: (data: any) => {
        this.applicant = data;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('KYC loading error:', err);
        this.errorMessage = 'Unable to load your documents. Please try again.';
        this.loading = false;
      }
    });
  }

  get kycStatus(): string {
    return this.applicant?.kyc_status || 'PENDING';
  }

  get uploadedCount(): number {
    return this.documents.filter(doc => this.isUploaded(doc.key)).length;
  }

  isUploaded(key: string): boolean {
    return !!this.applicant?.[key];
  }

  getDocumentStatus(key: string): string {
    if (!this.isUploaded(key)) {
      return 'Not Submitted';
    }

    if (this.kycStatus === 'APPROVED') {
      return 'Approved';
    }

    if (this.kycStatus === 'REJECTED') {
      return 'Rejected';
    }

    return 'Submitted - Pending Review';
  }

  openDocument(key: string): void {
    const path = this.applicant?.[key];

    if (!path) return;

    this.errorMessage = 'Document viewing is not yet available. Please contact support.';
  }
}
