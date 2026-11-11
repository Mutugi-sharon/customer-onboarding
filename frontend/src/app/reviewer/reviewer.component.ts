import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

type MenuOption = 'overview' | 'pending' | 'approved' | 'rejected' | 'all';

interface Applicant {
  id: string;
  fullName: string;
  email: string;
  phoneNumber: string;
  county?: string;
  town?: string;
  applicationStatus: string;
  createdAt?: string;
}

interface ApplicantDocument {
  label: string;
  type: string;
  path: string | null;
}

@Component({
  selector: 'app-reviewer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reviewer.component.html',
  styleUrl: './reviewer.component.css'
})
export class ReviewerComponent implements OnInit, OnDestroy {

  private apiUrl = 'http://localhost:8080/api/applicants';

  applicants: Applicant[] = [];
  filteredApplicants: Applicant[] = [];

  activeMenu: MenuOption = 'overview';
  searchTerm = '';

  loading = false;
  loadingDetails = false;
  submittingReview = false;

  selectedApplicant: Applicant | null = null;
  applicantDetails: any = null;

  documents: ApplicantDocument[] = [];

  showReviewPanel = false;
  showRejectForm = false;
  rejectionReason = '';

  previewUrl: SafeResourceUrl | null = null;
  previewDocumentName = '';
  private currentObjectUrl: string | null = null;

  successMessage = '';
  errorMessage = '';

  constructor(
    private http: HttpClient,
    private router: Router,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.loadApplicants();
  }

  get pageTitle(): string {
    const titles: Record<MenuOption, string> = {
      overview: 'Reviewer Dashboard',
      pending: 'Pending Applications',
      approved: 'Approved Applications',
      rejected: 'Rejected Applications',
      all: 'All Applications'
    };

    return titles[this.activeMenu];
  }

  get totalCount(): number {
    return this.applicants.length;
  }

  get pendingCount(): number {
    return this.applicants.filter(a =>
      this.normalizeStatus(a.applicationStatus) === 'PENDING_REVIEW'
    ).length;
  }

  get approvedCount(): number {
    return this.applicants.filter(a =>
      this.normalizeStatus(a.applicationStatus) === 'APPROVED'
    ).length;
  }

  get rejectedCount(): number {
    return this.applicants.filter(a =>
      this.normalizeStatus(a.applicationStatus) === 'REJECTED'
    ).length;
  }

  private normalizeStatus(status: string): string {
    const normalized = String(status || '').trim().toUpperCase();

    if (normalized === 'PENDING') {
      return 'PENDING_REVIEW';
    }

    return normalized;
  }

  loadApplicants(): void {
    this.loading = true;
    this.errorMessage = '';

    this.http.get<any[]>(`${this.apiUrl}/list`).subscribe({
      next: (response) => {
        this.applicants = (response || []).map(item => ({
          id: item.id,
          fullName: item.full_name || item.fullName || 'Not available',
          email: item.email || 'Not available',
          phoneNumber: item.phone_number || item.phoneNumber || '',
          county: item.county,
          town: item.town,
          applicationStatus: this.normalizeStatus(
            item.application_status || item.applicationStatus || 'PENDING_REVIEW'
          ),
          createdAt: item.created_at || item.createdAt
        }));

        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load applications:', error);
        this.errorMessage = 'Unable to load applications. Please try again.';
        this.loading = false;
      }
    });
  }

  selectMenu(menu: MenuOption): void {
    this.activeMenu = menu;
    this.applyFilters();
    this.successMessage = '';
    this.errorMessage = '';
  }

  onSearch(): void {
    this.applyFilters();
  }

  private applyFilters(): void {
    let result = [...this.applicants];

    if (this.activeMenu === 'pending') {
      result = result.filter(a =>
        this.normalizeStatus(a.applicationStatus) === 'PENDING_REVIEW'
      );
    } else if (this.activeMenu === 'approved') {
      result = result.filter(a =>
        this.normalizeStatus(a.applicationStatus) === 'APPROVED'
      );
    } else if (this.activeMenu === 'rejected') {
      result = result.filter(a =>
        this.normalizeStatus(a.applicationStatus) === 'REJECTED'
      );
    }

    const search = this.searchTerm.trim().toLowerCase();

    if (search) {
      result = result.filter(a =>
        String(a.id).toLowerCase().includes(search) ||
        a.fullName.toLowerCase().includes(search) ||
        a.email.toLowerCase().includes(search)
      );
    }

    this.filteredApplicants = result;
  }

  reviewApplicant(applicant: Applicant): void {
    this.selectedApplicant = applicant;
    this.showReviewPanel = true;
    this.loadingDetails = true;
    this.applicantDetails = null;
    this.documents = [];
    this.cancelRejection();

    this.http.get<any>(`${this.apiUrl}/${applicant.id}`).subscribe({
      next: (data) => {
        this.applicantDetails = data;

        this.documents = [
          {
            label: 'National ID',
            type: 'national_id',
            path: data.national_id_link || data.nationalIdLink || null
          },
          {
            label: 'KRA PIN Certificate',
            type: 'kra',
            path: data.kra_link || data.kraLink || null
          },
          {
            label: 'Passport Photo',
            type: 'photo',
            path: data.photo_link || data.photoLink || null
          },
          {
            label: 'Proof of Address',
            type: 'address_proof',
            path: data.address_proof_link || data.addressProofLink || null
          },
          {
            label: 'Proof of Income',
            type: 'income',
            path: data.income_link || data.incomeLink || null
          },
          {
            label: 'Employment Letter',
            type: 'employment_letter',
            path: data.employment_letter_link || data.employmentLetterLink || null
          }
        ];

        this.loadingDetails = false;
      },
      error: (error) => {
        console.error('Failed to load applicant details:', error);
        this.errorMessage = 'Unable to load applicant details.';
        this.loadingDetails = false;
      }
    });
  }

  rejectApplicant(): void {
    this.showRejectForm = true;
    this.rejectionReason = '';
    this.errorMessage = '';
    this.successMessage = '';
  }

  onRejectionReasonChange(event: Event): void {
    const target = event.target as HTMLTextAreaElement;
    this.rejectionReason = target.value;
  }

  cancelRejection(): void {
    this.showRejectForm = false;
    this.rejectionReason = '';
  }

  confirmRejection(): void {
    const reason = this.rejectionReason.trim();

    if (!reason) {
      this.errorMessage = 'Please enter a reason for rejecting this application.';
      return;
    }

    this.updateApplicationStatus('REJECTED', reason);
  }

  approveApplicant(): void {
    this.updateApplicationStatus('APPROVED');
  }

  private updateApplicationStatus(
    status: string,
    reason: string | null = null
  ): void {
    if (!this.selectedApplicant || this.submittingReview) {
      return;
    }

    this.submittingReview = true;
    this.errorMessage = '';
    this.successMessage = '';

    const applicantId = this.selectedApplicant.id;

    this.http.patch(
      `${this.apiUrl}/${applicantId}/review`,
      { status, reason }
    ).subscribe({
      next: () => {
        this.successMessage = status === 'APPROVED'
          ? 'Application approved successfully.'
          : 'Application rejected. The reviewer comment has been submitted.';

        this.submittingReview = false;
        this.closeReview();
        this.loadApplicants();
      },
      error: (error) => {
        console.error('Failed to update application:', error);
        this.errorMessage =
          error?.error?.message ||
          'Unable to update application. Please try again.';
        this.submittingReview = false;
      }
    });
  }

  openDocument(document: ApplicantDocument): void {
    if (!this.selectedApplicant || !document.path) {
      return;
    }

    this.clearPreview();

    this.previewDocumentName = document.label;

    this.http.get(
      `${this.apiUrl}/${this.selectedApplicant.id}/documents/${document.type}`,
      { responseType: 'blob' }
    ).subscribe({
      next: (blob) => {
        this.currentObjectUrl = URL.createObjectURL(blob);

        this.previewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
          this.currentObjectUrl
        );
      },
      error: (error) => {
        console.error('Failed to open document:', error);
        this.errorMessage = `Unable to open ${document.label}.`;
      }
    });
  }

  closePreview(): void {
    this.clearPreview();
  }

  private clearPreview(): void {
    if (this.currentObjectUrl) {
      URL.revokeObjectURL(this.currentObjectUrl);
      this.currentObjectUrl = null;
    }

    this.previewUrl = null;
    this.previewDocumentName = '';
  }

  closeReview(): void {
    this.showReviewPanel = false;
    this.showRejectForm = false;
    this.rejectionReason = '';
    this.selectedApplicant = null;
    this.applicantDetails = null;
    this.documents = [];
    this.clearPreview();
  }

  logout(): void {
    localStorage.removeItem('kcb.reviewer.token');
    localStorage.removeItem('kcb.reviewer.id');
    localStorage.removeItem('kcb.reviewer.name');
    localStorage.removeItem('kcb.reviewer.role');

    this.router.navigate(['/reviewer-login']);
  }

  ngOnDestroy(): void {
    this.clearPreview();
  }
}
