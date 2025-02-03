import { Routes } from '@angular/router';
import { AddDataComponent } from './add-data/add-data.component';
import { UpdateDataComponent } from './update-data/update-data.component';
import { DeleteDataComponent } from './delete-data/delete-data.component';
import { DashboardComponent } from './dashboard/dashboard.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'add', component: AddDataComponent },
  { path: 'update', component: UpdateDataComponent },
  { path: 'delete', component: DeleteDataComponent },
  { path: 'update/:showId', component: UpdateDataComponent },
  { path: 'delete/:showId', component: DeleteDataComponent }
];
