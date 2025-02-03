import { Component } from '@angular/core';
import { DataService } from '../services/data.service';
import { NetflixTitle } from '../models/netflix-title.model';

@Component({
  selector: 'app-add-data',
  templateUrl: './add-data.component.html',
  styleUrls: ['./add-data.component.css']
})
export class AddDataComponent {
  newTitle: NetflixTitle = this.getEmptyTitle();

  constructor(private dataService: DataService) { }

  onSubmit(): void {
    this.dataService.createTitle(this.newTitle).subscribe(() => {
      alert('Data added successfully!');
      this.resetForm();
    });
  }

  resetForm(): void {
    this.newTitle = this.getEmptyTitle();
  }

  getEmptyTitle(): NetflixTitle {
    return {
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
  }
}
