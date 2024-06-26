import { Component, Injector, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { StripeEmbeddedCheckout } from '@stripe/stripe-js';
import { StripeCardComponent, StripeService } from 'ngx-stripe';
import { DialogService, OButtonComponent, ODialogConfig, OFormComponent, OntimizeService, ServiceResponse } from 'ontimize-web-ngx';

@Component({
  selector: 'app-stripe',
  templateUrl: './stripe.component.html',
  styleUrls: ['./stripe.component.scss']
})
export class StripeComponent implements OnInit, OnDestroy {
  [x: string]: any;

  // ====================== Variables ======================
  public loading: boolean = false;
  public isCheckingOut: boolean = false;
  private baseUrl: string;
  private checkoutElement: HTMLElement;

  // ====================== Stripe Variables ======================
  private checkout: StripeEmbeddedCheckout;

  // ====================== Inputs ======================
  @Input('toyId') toyId: string;
  @Input('product') product: string;
  @Input('email') email: string;
  @Input('amount') amount: number;

  // ====================== SIMPLE CARD ELEMENT ======================
  @ViewChild(StripeCardComponent) cardElement!: StripeCardComponent;
  @ViewChild('stripeForm') protected stripeForm: OFormComponent;
  @ViewChild('payButton') protected payButton: OButtonComponent;

  // ====================== Ontimize Variables ======================
  ontimizeService: OntimizeService;

  constructor(
    private stripeService: StripeService,
    protected injector: Injector,
    protected dialogService: DialogService,
    private router: Router
  ) {
    this.ontimizeService = this.injector.get(OntimizeService);
    this.configureService();
  }
  protected configureService() {
    // Configure the service using the configuration defined in the `app.services.config.ts` file
    const conf = this.ontimizeService.getDefaultServiceConfiguration('payments');
    this.ontimizeService.configureService(conf);
  }

  ngOnInit(): void {

  }

  //====================== STRIPE CHECKOUT JS =======================

  checkoutStripe(shipment: boolean): void {
    this.baseUrl = window.location.origin;
    if( this.baseUrl.includes('localhost') ) {
      this.baseUrl = 'http://localhost:8080';
    }

    this.isCheckingOut = true;
    this.loading = true;

    let data = {
      'shipping': shipment,
      'toyid': this.toyId,
      'toyUrl': this.baseUrl + '/main/toys/toysDetail/' + this.toyId
    }


    this.ontimizeService.doRequest({
      method: 'POST',
      url: `${this.baseUrl}/payments/create-checkout-session`,
      body: data,
      options: {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    }).subscribe({
      next: async (session: ServiceResponse) => {

        let sesiondata = JSON.parse(session.data.session);

        await this.stripeService.getInstance().initEmbeddedCheckout({
          clientSecret: sesiondata.client_secret
        }).then((checkout: StripeEmbeddedCheckout) => {
          this.checkout = checkout;
          this.checkout.mount(
            '#checkout'
          );
          this.loading = false;
        }).catch((err) => {
          console.error(err);
          this.showCustom('error', 'OK', 'Error en embedded checkout', err);

        });

      },
      error: (err: any) => {
        console.error(err);
        this.showCustom('error', 'OK', 'Error en endpoint', err.error.data.error, "/");

      }
    })
  }

  ngOnDestroy(): void {
    try {
      this.checkout.destroy();
    } catch (e ){

    }
  }

  // =============================  DIALOGS =============================

  showCustom(
    icon: string = 'info',
    btnText: string,
    dialogTitle: string,
    dialogText: string,
    redirectUrl?: string
  ) {
    if (this.dialogService) {
      const config: ODialogConfig = {
        icon: icon,
        okButtonText: btnText
      };

      this.dialogService.alert(dialogTitle, dialogText, config);
      this.dialogService.dialogRef.afterClosed().subscribe( result => {
        if(result) {
          this.router.navigate(["main"], { replaceUrl: true });
        }
      });
    }
  }
}
