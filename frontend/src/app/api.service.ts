import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private base = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getGenders(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/api/applicants/gender`);
  }

  getNationalities(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/api/applicants/nationalities`);
  }

  registerApplicant(payload: any): Observable<{ applicationId: string; message: string }> {
    return this.http.post<{ applicationId: string; message: string }>(
      `${this.base}/api/applicants/register`,
      payload
    );
  }

  uploadDocuments(applicantId: string, formData: FormData): Observable<any> {
    return this.http.post<any>(
      `${this.base}/api/applicants/${applicantId}/documents`,
      formData
    );
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.base}/api/auth/login`, { email, password });
  }

  getApplicant(id: string): Observable<any> {
    return this.http.get<any>(`${this.base}/api/applicants/${id}`);
  }
}
