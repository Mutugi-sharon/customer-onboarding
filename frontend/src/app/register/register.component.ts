
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {

  step = 1;
  loading = false;
  errorMessage = '';
  successMessage = '';

  fullName = '';
  dateOfBirth = '';
  nationality = 'Kenyan';
  gender = '';
  identificationNumber = '';

  email = '';
  countryCode = '+254';
  phone = '';
  county = '';
  town = '';

  password = '';
  confirmPassword = '';

  occupation = '';
  employer = '';
  monthlyIncome = '';

  showPassword = false;
  showConfirmPassword = false;

  idDocument: File | null = null;
  kraDocument: File | null = null;
  passportPhoto: File | null = null;
  incomeDocument: File | null = null;
  employmentLetter: File | null = null;

  countryCodes = [
    { label: 'Kenya', code: '+254', digits: 9 },
    { label: 'Uganda', code: '+256', digits: 9 },
    { label: 'Tanzania', code: '+255', digits: 9 },
    { label: 'Rwanda', code: '+250', digits: 9 }
  ];

  constructor(
    private router: Router,
    private api: ApiService
  ) {}

  get selectedCountry() {
    return this.countryCodes.find(c => c.code === this.countryCode);
  }

  get emailValid() {
    return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(this.email.trim());
  }

  get nationalIdValid(): boolean {
    return /^\d{9}$/.test(this.identificationNumber);
  }

  onIdentificationNumberChange(value: string) {
    this.identificationNumber = value.replace(/\D/g, '').slice(0, 9);
  }

  get phoneValid() {
    return this.selectedCountry
      ? /^\d+$/.test(this.phone) &&
      this.phone.length === this.selectedCountry.digits
      : false;
  }

  get passwordValid() {
    return this.password.length >= 8 &&
      /[A-Za-z]/.test(this.password) &&
      /\d/.test(this.password);
  }

  get passwordsMatch() {
    return this.password === this.confirmPassword &&
      this.confirmPassword.length > 0;
  }

  get fullInternationalPhone() {
    return `${this.countryCode}${this.phone}`;
  }

  get documentsValid() {
    return !!this.idDocument &&
      !!this.kraDocument &&
      !!this.passportPhoto;
  }

  nextStep() {
    this.errorMessage = '';

    if (this.step === 1) {
      if (
        !this.fullName.trim() ||
        !this.dateOfBirth ||
        !this.gender ||
        !this.nationalIdValid
      ) {
        this.errorMessage =
          'Complete all fields and enter a valid 9-digit National ID.';
        return;
      }

      this.step = 2;

    } else if (this.step === 2) {
      if (
        !this.emailValid ||
        !this.phoneValid ||
        !this.county.trim() ||
        !this.town.trim()
      ) {
        this.errorMessage = 'Check your contact details.';
        return;
      }

      this.step = 3;

    } else if (this.step === 3) {
      if (!this.passwordValid || !this.passwordsMatch) {
        this.errorMessage = 'Check your password.';
        return;
      }

      this.step = 4;

    } else if (this.step === 4) {
      if (!this.documentsValid) {
        this.errorMessage =
          'Upload your National ID, KRA PIN Certificate and Passport Photo.';
        return;
      }

      this.step = 5;
    }
  }

  previousStep() {
    if (this.step > 1) {
      this.step--;
      this.errorMessage = '';
    }
  }

  editStep(targetStep: number) {
    if (targetStep >= 1 && targetStep <= 4) {
      this.step = targetStep;
      this.errorMessage = '';
      this.successMessage = '';
    }
  }

  onFileSelected(event: Event, type: string) {
    const input = event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      return;
    }

    const file = input.files[0];

    if (file.size > 5 * 1024 * 1024) {
      this.errorMessage = 'File too large (max 5MB).';
      input.value = '';
      return;
    }

    this.errorMessage = '';

    if (type === 'id') this.idDocument = file;
    else if (type === 'kra') this.kraDocument = file;
    else if (type === 'photo') this.passportPhoto = file;
    else if (type === 'income') this.incomeDocument = file;
    else if (type === 'employment') this.employmentLetter = file;
  }

  removeDocument(type: string) {
    if (type === 'id') this.idDocument = null;
    else if (type === 'kra') this.kraDocument = null;
    else if (type === 'photo') this.passportPhoto = null;
    else if (type === 'income') this.incomeDocument = null;
    else if (type === 'employment') this.employmentLetter = null;
  }

  getFileName(file: File | null) {
    return file ? file.name : '';
  }

  submitRegistration() {
    if (this.loading) {
      return;
    }

    if (!this.documentsValid) {
      this.errorMessage =
        'Upload your National ID, KRA PIN Certificate and Passport Photo.';
      this.step = 4;
      return;
    }

    if (!this.nationalIdValid) {
      this.errorMessage = 'National ID must contain exactly 9 digits.';
      this.step = 1;
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload = {
      fullName: this.fullName.trim(),
      dateOfBirth: this.dateOfBirth,
      nationality: this.nationality,
      gender: this.gender,
      identificationType: 'NATIONAL_ID',
      identificationNumber: this.identificationNumber.trim(),
      email: this.email.trim().toLowerCase(),
      phoneNumber: this.fullInternationalPhone,
      county: this.county.trim(),
      town: this.town.trim(),
      password: this.password,
    };

    this.api.registerApplicant(payload).subscribe({
      next: (res: any) => {
        const applicantId = res.applicationId;
        const fd = new FormData();

        if (this.idDocument) {
          fd.append('nationalId', this.idDocument);
        }

        if (this.kraDocument) {
          fd.append('kraCertificate', this.kraDocument);
        }

        if (this.passportPhoto) {
          fd.append('passportPhoto', this.passportPhoto);
        }

        if (this.incomeDocument) {
          fd.append('proofOfIncome', this.incomeDocument);
        }

        if (this.employmentLetter) {
          fd.append('employmentLetter', this.employmentLetter);
        }

        this.api.uploadDocuments(applicantId, fd).subscribe({
          next: () => {
            this.loading = false;

            this.successMessage =
              'Application submitted successfully! Your application has been received. You can now log in to track its progress.';

            this.errorMessage = '';
          },

          error: () => {
            this.loading = false;
            this.errorMessage =
              'Your application was created, but document upload failed. Please contact support before submitting again.';
          }
        });
      },

      error: (err: any) => {
        this.loading = false;
        this.errorMessage =
          err.error?.message || 'Registration failed.';
      }
    });
  }

  goHome() {
    this.router.navigate(['/']);
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
