import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NetflixTitle } from '../models/netflix-title.model';

@Injectable({
  providedIn: 'root'
})
export class DataService {
  private baseUrl = '/api/titles';

  constructor(private http: HttpClient) {}

  getAllTitles(): Observable<NetflixTitle[]> {
    return this.http.get<NetflixTitle[]>(this.baseUrl);
  }

  getTitleById(show_id: string): Observable<NetflixTitle> {
    return this.http.get<NetflixTitle>(`${this.baseUrl}/${show_id}`);
  }

  createTitle(title: NetflixTitle): Observable<NetflixTitle> {
    return this.http.post<NetflixTitle>(this.baseUrl, title);
  }

  updateTitle(show_id: string, title: NetflixTitle): Observable<NetflixTitle> {
    return this.http.put<NetflixTitle>(`${this.baseUrl}/${show_id}`, title);
  }

  deleteTitle(show_id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${show_id}`);
  }
}
