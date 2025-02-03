import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../services/api.service';
import { firstValueFrom } from 'rxjs';
import { ActivatedRoute } from "@angular/router";

@Component({
  selector: 'app-delete-data',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './delete-data.component.html',
  styleUrls: ['./delete-data.component.css']
})
export class DeleteDataComponent {
  showId: string = '';
  showDetails: any = null;
  errorMessage: string = '';
  successMessage: string = '';
  loading: boolean = false;

  constructor(
    private apiService: ApiService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.showId = params['showId'];
      if (this.showId) {
        this.searchShowById();
      }
    });
  }

  async searchShowById() {
    if (!this.showId) {
      this.errorMessage = 'Please enter a Show ID.';
      this.showTemporaryMessage('errorMessage');
      return;
    }

    this.loading = true;
    try {
      this.showDetails = await firstValueFrom(this.apiService.getTitleById(this.showId));
      this.errorMessage = '';
    } catch (error) {
      // @ts-ignore
      if (error.status === 404) {
        this.errorMessage = 'Show ID does not exist.';
      } else {
        // @ts-ignore
        this.errorMessage = 'Failed to fetch show details. ' + error.message;
      }
      this.showDetails = null;
      this.showTemporaryMessage('errorMessage');
    } finally {
      this.loading = false;
    }
  }

  async deleteShow() {
    if (confirm('Are you sure you want to delete this show?')) {
      this.loading = true;
      try {
        await firstValueFrom(this.apiService.deleteTitle(this.showId));
        this.successMessage = 'Show deleted successfully!';
        this.errorMessage = '';
        this.showDetails = null;
        this.showTemporaryMessage('successMessage');
      } catch (error) {
        this.errorMessage = 'Failed to delete show.';
        this.successMessage = '';
        this.showTemporaryMessage('errorMessage');
      } finally {
        this.loading = false;
      }
    }
  }

  showTemporaryMessage(type: 'errorMessage' | 'successMessage') {
    setTimeout(() => {
      this[type] = '';
    }, 3000);
  }
}
