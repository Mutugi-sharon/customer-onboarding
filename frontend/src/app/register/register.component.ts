
import { Component, OnDestroy } from '@angular/core';
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
export class RegisterComponent implements OnDestroy {

  step = 1;
  loading = false;
  errorMessage = '';
  successMessage = '';

  // Field-specific validation errors
  fieldErrors: { [key: string]: string } = {};
  submitted = false;

  // OTP properties
  otpStage = false;
  otpCode = '';
  otpDigits: string[] = ['', '', '', '', '', ''];
  verificationToken = '';
  resendCooldown = 0;
  private cooldownTimer: ReturnType<typeof setInterval> | null = null;

  // Name properties
  firstName = '';
  middleName = '';
  lastName = '';

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

  // Duplicate checking
  emailExists = false;
  phoneExists = false;
  identificationNumberExists = false;

  // Uploaded documents
  idDocument: File | null = null;
  kraDocument: File | null = null;
  passportPhoto: File | null = null;
  incomeDocument: File | null = null;
  employmentLetter: File | null = null;

  private readonly MAX_FILE_SIZE = 5 * 1024 * 1024;

  private readonly allowedDocumentExtensions = [
    'pdf', 'png', 'jpg', 'jpeg'
  ];

  private readonly allowedImageExtensions = [
    'png', 'jpg', 'jpeg'
  ];

  private readonly allowedDocumentMimeTypes = [
    'application/pdf', 'image/png', 'image/jpeg'
  ];

  private readonly allowedImageMimeTypes = [
    'image/png', 'image/jpeg'
  ];

  countryCodes = [
    { label: 'Kenya', code: '+254', digits: 9 },
    { label: 'Uganda', code: '+256', digits: 9 },
    { label: 'Tanzania', code: '+255', digits: 9 },
    { label: 'Rwanda', code: '+250', digits: 9 }
  ];

  // County and town dropdown data
  counties: { name: string; towns: string[] }[] = [
    {
      name: 'Nairobi',
      towns: ['Westlands', 'Kilimani', 'Karen', 'Kahawa', 'CBD', 'Embakasi']
    },
    {
      name: 'Kiambu',
      towns: ['Thika', 'Ruiru', 'Kiambu', 'Kikuyu', 'Limuru', 'Juja']
    },
    {
      name: 'Machakos',
      towns: ['Machakos', 'Athi River', 'Mlolongo', 'Kangundo', 'Tala']
    },
    {
      name: 'Kajiado',
      towns: ['Kitengela', 'Ngong', 'Kajiado', 'Ongata Rongai', 'Isinya']
    },
    {
      name: 'Nakuru',
      towns: ['Nakuru', 'Naivasha', 'Gilgil', 'Molo', 'Njoro']
    },
    {
      name: 'Mombasa',
      towns: ['Mombasa', 'Likoni', 'Bamburi', 'Kisauni', 'Nyali']
    },
    {
      name: 'Kisumu',
      towns: ['Kisumu', 'Ahero', 'Maseno', 'Muhoroni']
    },
    {
      name: 'Uasin Gishu',
      towns: ['Eldoret', 'Burnt Forest', 'Turbo', 'Moiben']
    },
    {
      name: 'Meru',
      towns: ['Meru', 'Maua', 'Nkubu', 'Timau']
    },
    {
      name: 'Nyeri',
      towns: ['Nyeri', 'Karatina', 'Othaya', 'Mukurweini']
    }
  ];

  constructor(
    private router: Router,
    private api: ApiService
  ) {}

  get fullName(): string {
    return [this.firstName, this.middleName, this.lastName]
      .filter(name => name.trim().length > 0)
      .join(' ');
  }

  get selectedCountry() {
    return this.countryCodes.find(c => c.code === this.countryCode);
  }

  get selectedCounty() {
    return this.counties.find(c => c.name === this.county);
  }

  get availableTowns(): string[] {
    return this.selectedCounty?.towns || [];
  }

  get emailValid(): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(this.email.trim());
  }

  get nationalIdValid(): boolean {
    return /^\d{9}$/.test(this.identificationNumber);
  }

  get phoneValid(): boolean {
    return this.selectedCountry
      ? /^\d+$/.test(this.phone) &&
      this.phone.length === this.selectedCountry.digits
      : false;
  }

  get passwordValid(): boolean {
    return this.password.length >= 8 &&
      /[A-Za-z]/.test(this.password) &&
      /\d/.test(this.password);
  }

  get passwordsMatch(): boolean {
    return this.password === this.confirmPassword &&
      this.confirmPassword.length > 0;
  }

  get fullInternationalPhone(): string {
    return `${this.countryCode}${this.phone}`;
  }

  get documentsValid(): boolean {
    return !!this.idDocument &&
      !!this.kraDocument &&
      !!this.passportPhoto;
  }

  get otpComplete(): boolean {
    return this.otpDigits.every(digit => /^\d$/.test(digit));
  }

  // OTP helpers
  updateOtpCode() {
    this.otpCode = this.otpDigits.join('');
  }

  onOtpInput(index: number, value: string, input: HTMLInputElement) {
    const digit = value.replace(/\D/g, '').slice(-1);

    this.otpDigits[index] = digit;
    this.updateOtpCode();

    if (digit && index < 5) {
      const nextInput = document.getElementById(
        `otp-${index + 1}`
      ) as HTMLInputElement;

      nextInput?.focus();
    }

    this.errorMessage = '';
  }

  onOtpKeydown(index: number, event: KeyboardEvent) {
    const input = event.target as HTMLInputElement;

    if (event.key === 'Backspace' && !input.value && index > 0) {
      const previousInput = document.getElementById(
        `otp-${index - 1}`
      ) as HTMLInputElement;

      previousInput?.focus();
    }
  }

  onOtpPaste(event: ClipboardEvent) {
    event.preventDefault();

    const pasted = event.clipboardData
      ?.getData('text')
      .replace(/\D/g, '')
      .slice(0, 6) || '';

    this.otpDigits = ['', '', '', '', '', ''];

    pasted.split('').forEach((digit, index) => {
      this.otpDigits[index] = digit;
    });

    this.updateOtpCode();

    const nextIndex = Math.min(pasted.length, 5);

    const input = document.getElementById(
      `otp-${nextIndex}`
    ) as HTMLInputElement;

    input?.focus();
  }

  // County and town
  onCountyChange() {
    this.town = '';
    delete this.fieldErrors['county'];
    delete this.fieldErrors['town'];
  }

  // Field changes
  onIdentificationNumberChange(value: string) {
    this.identificationNumber = value.replace(/\D/g, '').slice(0, 9);
    this.identificationNumberExists = false;
    delete this.fieldErrors['identificationNumber'];
  }

  onEmailChange() {
    this.emailExists = false;
    delete this.fieldErrors['email'];
  }

  onPhoneChange() {
    this.phoneExists = false;
    delete this.fieldErrors['phone'];
  }

  // Document validation
  validateDocument(file: File, type: string): string | null {
    if (file.size > this.MAX_FILE_SIZE) {
      return 'File too large. Maximum allowed size is 5MB per document.';
    }

    if (file.size === 0) {
      return 'The selected file is empty. Please choose another file.';
    }

    const extension = file.name.split('.').pop()?.toLowerCase();
    const isPassportPhoto = type === 'photo';

    const allowedExtensions = isPassportPhoto
      ? this.allowedImageExtensions
      : this.allowedDocumentExtensions;

    const allowedMimeTypes = isPassportPhoto
      ? this.allowedImageMimeTypes
      : this.allowedDocumentMimeTypes;

    if (!extension || !allowedExtensions.includes(extension)) {
      return isPassportPhoto
        ? 'Passport photo must be PNG, JPG or JPEG.'
        : 'Unsupported file format. Please upload PDF, PNG, JPG or JPEG.';
    }

    if (file.type && !allowedMimeTypes.includes(file.type.toLowerCase())) {
      return isPassportPhoto
        ? 'Invalid passport photo format. Use PNG, JPG or JPEG.'
        : 'Invalid document format. Use PDF, PNG, JPG or JPEG.';
    }

    return null;
  }

  // Clear field errors
  private clearFieldErrors() {
    this.fieldErrors = {};
  }

  // Step 1 validation
  private validateStepOne(): boolean {
    this.fieldErrors = {};

    if (!this.firstName.trim()) {
      this.fieldErrors['firstName'] = 'First name is required.';
    }

    if (!this.lastName.trim()) {
      this.fieldErrors['lastName'] = 'Last name is required.';
    }

    if (!this.dateOfBirth) {
      this.fieldErrors['dateOfBirth'] = 'Date of birth is required.';
    }

    if (!this.gender) {
      this.fieldErrors['gender'] = 'Please select your gender.';
    }

    if (!this.nationalIdValid) {
      this.fieldErrors['identificationNumber'] =
        'National ID must contain exactly 9 digits.';
    }

    return Object.keys(this.fieldErrors).length === 0;
  }

  // Step 2 validation
  private validateStepTwo(): boolean {
    this.fieldErrors = {};

    if (!this.email.trim()) {
      this.fieldErrors['email'] = 'Email address is required.';
    } else if (!this.emailValid) {
      this.fieldErrors['email'] = 'Enter a valid email address.';
    }

    if (!this.phone) {
      this.fieldErrors['phone'] = 'Phone number is required.';
    } else if (!this.phoneValid) {
      this.fieldErrors['phone'] =
        `Enter a valid ${this.selectedCountry?.digits || 9}-digit phone number.`;
    }

    if (!this.county) {
      this.fieldErrors['county'] = 'Please select your county.';
    }

    if (!this.town) {
      this.fieldErrors['town'] = 'Please select your town.';
    }

    return Object.keys(this.fieldErrors).length === 0;
  }

  // Step 3 validation
  private validateStepThree(): boolean {
    this.fieldErrors = {};

    if (!this.password) {
      this.fieldErrors['password'] = 'Password is required.';
    } else if (!this.passwordValid) {
      this.fieldErrors['password'] =
        'Use at least 8 characters, including a letter and a number.';
    }

    if (!this.confirmPassword) {
      this.fieldErrors['confirmPassword'] =
        'Please confirm your password.';
    } else if (!this.passwordsMatch) {
      this.fieldErrors['confirmPassword'] = 'Passwords do not match.';
    }

    return Object.keys(this.fieldErrors).length === 0;
  }

  // Reusable duplicate checking
  private applyDuplicateErrors(res: any): boolean {
    this.emailExists = !!res.emailExists;
    this.phoneExists = !!res.phoneExists;
    this.identificationNumberExists =
      !!res.identificationNumberExists;

    this.fieldErrors = {};

    if (this.emailExists) {
      this.fieldErrors['email'] = 'Email already exists.';
    }

    if (this.phoneExists) {
      this.fieldErrors['phone'] = 'Phone number already exists.';
    }

    if (this.identificationNumberExists) {
      this.fieldErrors['identificationNumber'] =
        'National ID already exists.';
    }

    if (this.identificationNumberExists) {
      this.step = 1;
    } else if (this.emailExists || this.phoneExists) {
      this.step = 2;
    }

    return this.emailExists ||
      this.phoneExists ||
      this.identificationNumberExists;
  }

  // Move between steps
  nextStep() {
    this.errorMessage = '';

    if (this.step === 1) {
      if (!this.validateStepOne()) return;

      this.step = 2;

    } else if (this.step === 2) {
      if (!this.validateStepTwo()) return;

      this.checkDuplicatesBeforeNextStep();

    } else if (this.step === 3) {
      if (!this.validateStepThree()) return;

      this.step = 4;

    } else if (this.step === 4) {
      this.fieldErrors = {};

      if (!this.documentsValid) {
        this.errorMessage =
          'Upload your National ID, KRA PIN Certificate and Passport Photo.';
        return;
      }

      this.step = 5;
    }
  }

  // Check duplicates before moving to Security
  private checkDuplicatesBeforeNextStep() {
    if (this.loading) return;

    this.loading = true;
    this.errorMessage = '';

    this.api.checkDuplicates(
      this.email,
      this.fullInternationalPhone,
      this.identificationNumber
    ).subscribe({
      next: (res) => {
        this.loading = false;

        if (this.applyDuplicateErrors(res)) {
          return;
        }

        this.step = 3;
      },
      error: () => {
        this.loading = false;
        this.errorMessage =
          'Something went wrong. Please try again.';
      }
    });
  }

  previousStep() {
    if (this.step > 1) {
      this.step--;
      this.errorMessage = '';
      this.clearFieldErrors();
    }
  }

  editStep(targetStep: number) {
    if (targetStep >= 1 && targetStep <= 4) {
      this.step = targetStep;
      this.errorMessage = '';
      this.successMessage = '';
      this.clearFieldErrors();
    }
  }

  // Document selection
  onFileSelected(event: Event, type: string) {
    const input = event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];

    const allowedTypes = [
      'id', 'kra', 'photo', 'income', 'employment'
    ];

    if (!allowedTypes.includes(type)) {
      this.errorMessage = 'Invalid document upload field.';
      input.value = '';
      return;
    }

    const validationError = this.validateDocument(file, type);

    if (validationError) {
      this.errorMessage = validationError;
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

    this.errorMessage = '';
  }

  getFileName(file: File | null): string {
    return file ? file.name : '';
  }

  // Submit registration
  submitRegistration() {
    if (this.loading) return;

    if (!this.documentsValid) {
      this.errorMessage =
        'Upload your National ID, KRA PIN Certificate and Passport Photo.';
      this.step = 4;
      return;
    }

    if (!this.validateStepOne()) {
      this.step = 1;
      return;
    }

    if (!this.validateStepTwo()) {
      this.step = 2;
      return;
    }

    if (!this.validateStepThree()) {
      this.step = 3;
      return;
    }

    // Check duplicates again before requesting OTP
    this.loading = true;
    this.errorMessage = '';

    this.api.checkDuplicates(
      this.email,
      this.fullInternationalPhone,
      this.identificationNumber
    ).subscribe({
      next: (res) => {
        if (this.applyDuplicateErrors(res)) {
          this.loading = false;
          return;
        }

        this.requestOtp();
      },
      error: () => {
        this.loading = false;
        this.errorMessage =
          'Something went wrong. Please try again.';
      }
    });
  }

  // Request OTP
  private requestOtp() {
    this.api.sendOtp(this.email).subscribe({
      next: () => {
        this.loading = false;
        this.otpStage = true;
        this.otpCode = '';
        this.otpDigits = ['', '', '', '', '', ''];
        this.verificationToken = '';
        this.startResendCooldown();
      },
      error: () => {
        this.loading = false;
        this.errorMessage =
          'Unable to send verification code. Please try again.';
      }
    });
  }

  // OTP resend cooldown
  private startResendCooldown() {
    if (this.cooldownTimer) {
      clearInterval(this.cooldownTimer);
    }

    this.resendCooldown = 60;

    this.cooldownTimer = setInterval(() => {
      this.resendCooldown--;

      if (this.resendCooldown <= 0) {
        this.resendCooldown = 0;

        if (this.cooldownTimer) {
          clearInterval(this.cooldownTimer);
          this.cooldownTimer = null;
        }
      }
    }, 1000);
  }

  resendOtp() {
    if (this.loading || this.resendCooldown > 0) return;

    this.loading = true;
    this.errorMessage = '';

    this.api.sendOtp(this.email).subscribe({
      next: () => {
        this.loading = false;
        this.otpCode = '';
        this.otpDigits = ['', '', '', '', '', ''];
        this.startResendCooldown();
      },
      error: () => {
        this.loading = false;
        this.errorMessage =
          'Unable to resend verification code. Please try again.';
      }
    });
  }

  // Verify OTP
  verifyEmailOtp() {
    if (this.loading) return;

    this.updateOtpCode();

    if (!this.otpComplete) {
      this.errorMessage =
        'Enter the complete 6-digit verification code.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.api.verifyOtp(this.email, this.otpCode).subscribe({
      next: (res: any) => {
        this.verificationToken = res.verificationToken;

        if (!this.verificationToken) {
          this.loading = false;
          this.errorMessage =
            'Verification token was not received. Please try again.';
          return;
        }

        this.completeRegistration();
      },
      error: () => {
        this.loading = false;
        this.errorMessage =
          'Invalid or expired verification code.';
      }
    });
  }

  // Complete registration and upload documents
  private completeRegistration() {
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
      verificationToken: this.verificationToken
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
            this.otpStage = false;
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
      error: () => {
        this.loading = false;
        this.errorMessage =
          'Registration failed. Please try again.';
      }
    });
  }

  ngOnDestroy() {
    if (this.cooldownTimer) {
      clearInterval(this.cooldownTimer);
    }
  }

  goHome() {
    this.router.navigate(['/']);
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
