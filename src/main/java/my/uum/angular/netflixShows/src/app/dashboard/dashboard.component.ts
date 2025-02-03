import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../services/api.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  titles: any[] = [];
  loading = true;
  errorMessage: string | null = null;

  constructor(private apiService: ApiService, private router: Router) {}

  ngOnInit(): void {
    this.loadTitles();
  }

  async loadTitles() {
    this.loading = true;
    this.errorMessage = null;
    try {
      this.titles = await firstValueFrom(this.apiService.getAllTitles());
      this.titles.sort((a, b) => this.compareShowIds(a.show_id, b.show_id));
      console.log('Sorted titles:', this.titles);
    } catch (error) {
      this.errorMessage = 'Failed to fetch data';
      console.error('Error fetching titles:', error);
    } finally {
      this.loading = false;
    }
  }

  compareShowIds(id1: string, id2: string): number {
    const num1 = parseInt(id1.replace(/\D/g, ''), 10);
    const num2 = parseInt(id2.replace(/\D/g, ''), 10);
    return num1 - num2;
  }

  navigateToUpdate(showId: string) {
    this.router.navigate(['/update', showId]);
  }

  navigateToDelete(showId: string) {
    this.router.navigate(['/delete', showId]);
  }
}
