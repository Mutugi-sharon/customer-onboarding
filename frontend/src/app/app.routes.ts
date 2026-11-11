
import { Routes } from '@angular/router';

import { HomeComponent } from './home.component';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login.component';
import { DashboardComponent } from './dashboard.component';

import { ApplicationComponent } from './application/application.component';
import { KycComponent } from './kyc/kyc.component';
import { ProfileComponent } from './profile/profile.component';

import { ReviewerComponent } from './reviewer/reviewer.component';
import { ReviewerLoginComponent } from './reviewer-login/reviewer-login.component';

import { authGuard } from './auth.guard';

export const routes: Routes = [

  { path: '', component: HomeComponent },

  { path: 'register', component: RegisterComponent },

  { path: 'login', component: LoginComponent },

  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },

  {
    path: 'application',
    component: ApplicationComponent,
    canActivate: [authGuard]
  },

  {
    path: 'kyc',
    component: KycComponent,
    canActivate: [authGuard]
  },

  {
    path: 'profile',
    component: ProfileComponent,
    canActivate: [authGuard]
  },

  { path: 'reviewer-login', component: ReviewerLoginComponent },

  { path: 'reviewer', component: ReviewerComponent },

  { path: '**', redirectTo: '' }

];
