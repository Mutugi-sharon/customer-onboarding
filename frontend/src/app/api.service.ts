
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DuplicateCheckResponse {
  emailExists: boolean;
  phoneExists: boolean;
  identificationNumberExists: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private base = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getGenders(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.base}/api/applicants/gender`
    );
  }

  getNationalities(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.base}/api/applicants/nationalities`
    );
  }

  checkDuplicates(
    email: string,
    phoneNumber: string,
    identificationNumber: string
  ): Observable<DuplicateCheckResponse> {
    return this.http.get<DuplicateCheckResponse>(
      `${this.base}/api/applicants/check-duplicates`,
      {
        params: {
          email: email.trim().toLowerCase(),
          phoneNumber: phoneNumber.trim(),
          identificationNumber: identificationNumber.trim()
        }
      }
    );
  }

  sendOtp(email: string): Observable<any> {
    return this.http.post<any>(
      `${this.base}/api/otp/send`,
      {
        email: email.trim().toLowerCase()
      }
    );
  }

  verifyOtp(email: string, otp: string): Observable<any> {
    return this.http.post<any>(
      `${this.base}/api/otp/verify`,
      {
        email: email.trim().toLowerCase(),
        otp
      }
    );
  }

  registerApplicant(
    payload: any
  ): Observable<{ applicationId: string; message: string }> {
    return this.http.post<{ applicationId: string; message: string }>(
      `${this.base}/api/applicants/register`,
      payload
    );
  }

  uploadDocuments(
    applicantId: string,
    formData: FormData
  ): Observable<any> {
    return this.http.post<any>(
      `${this.base}/api/applicants/${applicantId}/documents`,
      formData
    );
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(
      `${this.base}/api/auth/login`,
      { email, password }
    );
  }

  getApplicant(id: string): Observable<any> {
    return this.http.get<any>(
      `${this.base}/api/applicants/${id}`
    );
  }
}
