import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { ApiService } from '../services/api.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-add-data',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-data.component.html',
  styleUrls: ['./add-data.component.css']
})
export class AddDataComponent implements OnInit {
  newShow = {
    show_id: '',
    type: '',
    title: '',
    director: '',
    cast: '',
    country: '',
    date_added: new Date(),
    release_year: 0,
    rating: '',
    duration: '',
    listed_in: '',
    description: ''
  };
  errorMessage: string = '';
  successMessage: string = '';
  loading: boolean = false;
  formVisible: boolean = false;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.generateShowId();
  }

  async generateShowId() {
    this.loading = true;
    try {
      const shows = await firstValueFrom(this.apiService.getAllTitles());
      const maxId = Math.max(...shows.map(show => parseInt(show.show_id.replace('s', ''), 10)));
      if (isNaN(maxId)) {
        throw new Error('No valid show IDs found');
      }
      this.newShow.show_id = 's' + (maxId + 1);
      this.formVisible = true;
    } catch (error) {
      this.errorMessage = 'Failed to generate Show ID. Please try again later.';
      this.formVisible = false;
      console.error('Error generating show ID:', error);
    } finally {
      this.loading = false;
    }
  }

  async addShow(form: NgForm) {
    if (form.valid) {
      this.loading = true;
      try {
        const shows = await firstValueFrom(this.apiService.getAllTitles());
        const showExists = shows.some(show => show.show_id === this.newShow.show_id);

        if (showExists) {
          this.errorMessage = 'Show ID already exists. Please try again.';
          this.successMessage = '';
          this.showTemporaryMessage('errorMessage');
        } else {
          await firstValueFrom(this.apiService.createTitle(this.newShow));
          this.successMessage = 'Show added successfully!';
          this.errorMessage = '';
          form.resetForm({
            show_id: this.newShow.show_id,
            date_added: new Date(),
            release_year: 0
          });
          this.showTemporaryMessage('successMessage', true);
        }
      } catch (error) {
        // @ts-ignore
        this.errorMessage = 'Failed to add show. ' + error.message;
        this.successMessage = '';
        this.showTemporaryMessage('errorMessage');
      } finally {
        this.loading = false;
      }
    } else {
      this.errorMessage = 'Please fill out all required fields.';
      this.showTemporaryMessage('errorMessage');
    }
  }

  showTemporaryMessage(type: 'errorMessage' | 'successMessage', reload: boolean = false) {
    setTimeout(() => {
      this[type] = '';
      if (reload && type === 'successMessage') {
        window.location.reload();
      }
    }, 3000);
  }
}
