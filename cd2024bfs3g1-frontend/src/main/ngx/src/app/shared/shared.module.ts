import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { OntimizeWebModule } from 'ontimize-web-ngx';
import { FilterComponent } from './components/filters/filters.component';
import { HomeToolbarComponent } from './components/home-toolbar/home-toolbar.component';
import { OMapModule } from 'ontimize-web-ngx-map';

export function calculateDistanceFunction(lat1:number, lon1:number, lat2:number, lon2:number) {
  const R:number = 6371; // Radio de la Tierra en kilómetros
  let dLat:number = deg2rad(lat2 - lat1);
  let dLon:number = deg2rad(lon2 - lon1);
  let a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
          + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
          * Math.sin(dLon / 2) * Math.sin(dLon / 2);
  let c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  let distance = R * c;
  return Math.round(distance * 100.0) / 100.0; // Redondear a 2 decimales
}

function deg2rad(deg:number) {
  return deg * (Math.PI / 180);
}

@NgModule({
  imports: [
    OntimizeWebModule,
    OMapModule
  ],
  declarations: [
    FilterComponent,
    HomeToolbarComponent
  ],
  exports: [
    CommonModule,
    FilterComponent,
    HomeToolbarComponent,
    OMapModule
  ]
})
export class SharedModule {

  
 }
