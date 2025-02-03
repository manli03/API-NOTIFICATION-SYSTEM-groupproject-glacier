import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getAllTitles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/all-titles`);
  }

  getTitleById(showId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/titles/${showId}`);
  }

  createTitle(title: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/titles`, title);
  }

  updateTitle(showId: string, title: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/titles/${showId}`, title);
  }

  deleteTitle(showId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/titles/${showId}`);
  }
}
