
import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const isReviewer = window.location.pathname.startsWith('/reviewer');

  const token = isReviewer
    ? localStorage.getItem('kcb.reviewer.token')
    : localStorage.getItem('kcb.token');

  if (!token) {
    return next(req);
  }

  const cloned = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(cloned);
};
