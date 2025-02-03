import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../services/api.service';
import { firstValueFrom } from 'rxjs';
import { ActivatedRoute } from "@angular/router";

@Component({
  selector: 'app-update-data',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './update-data.component.html',
  styleUrls: ['./update-data.component.css']
})
export class UpdateDataComponent {
  showId: string = '';
  showDetails: any = null;
  topErrorMessage: String = '';
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
      this.topErrorMessage = 'Please enter a Show ID.';
      this.showTemporaryMessage('topErrorMessage');
      return;
    }

    this.loading = true;
    try {
      this.showDetails = await firstValueFrom(this.apiService.getTitleById(this.showId));
      this.topErrorMessage = '';
    } catch (error) {
      // @ts-ignore
      if (error.status === 404) {
        this.topErrorMessage = 'Show ID does not exist.';
      } else {
        // @ts-ignore
        this.topErrorMessage = 'Failed to fetch show details. ' + error.message;
      }
      this.showDetails = null;
      this.showTemporaryMessage('topErrorMessage');
    } finally {
      this.loading = false;
    }
  }

  async updateShowDetails() {
    this.loading = true;
    try {
      await firstValueFrom(this.apiService.updateTitle(this.showId, this.showDetails));
      this.successMessage = 'Show updated successfully!';
      this.errorMessage = '';
      this.showTemporaryMessage('successMessage');
    } catch (error) {
      this.errorMessage = 'Failed to update show.';
      this.successMessage = '';
      this.showTemporaryMessage('errorMessage');
    } finally {
      this.loading = false;
    }
  }

  showTemporaryMessage(type: 'errorMessage' | 'successMessage' | 'topErrorMessage') {
    setTimeout(() => {
      this[type] = '';
      this.showDetails = null;
    }, 3000);
  }
}
