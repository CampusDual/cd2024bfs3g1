import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { OntimizeWebModule } from 'ontimize-web-ngx';
import { FilterComponent } from './components/filters/filters.component';
import { HomeToolbarComponent } from './components/home-toolbar/home-toolbar.component';
import { OMapModule } from 'ontimize-web-ngx-map';
import { StripeComponent } from './components/stripe/stripe.component';
import { NgxStripeModule } from 'ngx-stripe';
import { environment } from 'src/environments/environment';

@NgModule({
  imports: [
    OntimizeWebModule,
    OMapModule,
    NgxStripeModule.forRoot(environment.stripe_public_key),
  ],
  declarations: [
    FilterComponent,
    HomeToolbarComponent,
    StripeComponent
  ],
  exports: [
    CommonModule,
    FilterComponent,
    HomeToolbarComponent,
    OMapModule,
    StripeComponent
  ]
})
export class SharedModule {


 }
