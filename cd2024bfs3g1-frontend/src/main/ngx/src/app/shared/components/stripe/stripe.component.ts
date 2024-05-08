import { HttpClient } from '@angular/common/http';
import { Component, Injector, OnInit, ViewChild } from '@angular/core';
import { StripeCardElementOptions, StripeElementsOptions, PaymentIntent } from '@stripe/stripe-js';
import { StripeCardComponent, StripeService } from 'ngx-stripe';
import { OFormComponent, Observable, OntimizeService, ServiceResponse } from 'ontimize-web-ngx';
import { PaymentIntentDto } from './dto/payment-intent-dto';

@Component({
  selector: 'app-stripe',
  templateUrl: './stripe.component.html',
  styleUrls: ['./stripe.component.css']
})
export class StripeComponent {



  // ====================== SIMPLE CARD ELEMENT ======================
  @ViewChild(StripeCardComponent) cardElement!: StripeCardComponent;
  @ViewChild('stripeForm') protected stripeForm: OFormComponent;

  cardOptions: StripeCardElementOptions = {
    hidePostalCode: true,
    style: {
      base: {
        iconColor: '#666EE8',
        backgroundColor: '#F5F5F5',
        color: '#31325F',
        lineHeight: '40px',
        padding: '5px',
        fontWeight: '300',
        fontFamily: '"Helvetica Neue", Helvetica, sans-serif',
        fontSize: '18px',
        '::placeholder': {
          color: '#CFD7E0'
        }
      }
    }
  };

  ontimizeService: OntimizeService;

  constructor(
    private stripeService: StripeService,
    protected injector: Injector
  ) {
    this.ontimizeService = this.injector.get(OntimizeService);
    this.configureService();

  }
  protected configureService() {
    // Configure the service using the configuration defined in the `app.services.config.ts` file
    const conf = this.ontimizeService.getDefaultServiceConfiguration('test');
    this.ontimizeService.configureService(conf);
  }

  elementsOptions: StripeElementsOptions = {
    locale: 'es',
    currency: 'eur',
    amount: 5000,
    mode: 'payment'
  };


  pay() {
    let name = this.stripeForm.getFieldValue('product');
    let amount = this.stripeForm.getFieldValue('amount');

    this.ontimizeService.doRequest({
      method: 'GET',
      url: 'http://localhost:8080/test'     
    })
      // .subscribe( (resp ) => {
      //   console.log("doRequest:", );
      // })
      .subscribe({
        next: (resp: ServiceResponse) => {
          console.log("doRequest:", resp.data);
        }, error: (err) => {
          console.log("error:", err);
        }
      })

    // this.stripeService.createToken(this.cardElement.element, { name })
    //   .subscribe((result) => {
    //     if (result.token) {
    //       // Use the token
    //       let paymentIntentDto: PaymentIntentDto = {
    //         token: result.token.id,
    //         description: 'Descripcion test',
    //         amount: amount,
    //         currency: 'EUR'
    //       }

    //       this.paymentService.pagar(paymentIntentDto).subscribe(
    //         data => {
    //           this.abrirModal(data[`id`], this.nombre, data[`description`], data[`amount`]);
    //           this.router.navigate(['/']);
    //         }
    //       );
    //       this.error = undefined;
    //     } else if (result.error) {
    //       this.error = result.error.message;
    //     }
    //   });

    // console.log("name", name);
  }


  // pay(): void {

  //   console.log( "CardElement:", this.cardElement.element);


  //   this.createPaymentIntent(this.stripeTest.get('amount').value)
  //       .pipe(
  //         switchMap((pi) =>
  //           this.stripeService.confirmCardPayment(pi.client_secret, {
  //             payment_method: {
  //               card: this.cardElement.element,
  //               billing_details: {
  //                 name: this.stripeTest.get('name').value,
  //               },
  //             },
  //           })
  //         )
  //       )
  //       .subscribe((result) => {
  //         if (result.error) {
  //           // Show error to your customer (e.g., insufficient funds)
  //           console.log(result.error.message);
  //         } else {
  //           // The payment has been processed!
  //           if (result.paymentIntent.status === 'succeeded') {
  //             // Show a success message to your customer
  //           }
  //         }
  //       });

  // }

  // createPaymentIntent(amount: number): Observable<PaymentIntent> {

  //   return this.http.post<PaymentIntent>(
  //     `${env.apiUrl}/create-payment-intent`,
  //     { amount }
  //   );
  // }









  // ====================== STRIPE SIMPLE TOKENPK CARD ======================
  // Replace with your own public key
  // stripe = injectStripe("pk_test_51PBat3RpYL4S8tgEy69BoMSpHv1UraxL3lY3CYurMK5mx3InDvRCbu6Uzf9SxAz08C1V2Lr6K1gR7eYJSORYQxE600WVFUPXcv");

  // createToken() {

  //   // console.log("stripeForm: ", this.stripeForm.getFieldValues(['product', 'email']));
  //   let name = this.stripeForm.getFieldValues(['product']).product;
  //   let email = this.stripeForm.getFieldValues(['email']);
  //   let amount:number = parseInt(this.stripeForm.getFieldValues(['amount']).amount);

  //   console.log("Product: ", name);
  //   this.stripe
  //   .createToken(this.cardElement.element, { name })
  //   .subscribe((result) => {
  //     if (result.token) {
  //       console.log(result.token);
  //       // Use the token
  //       console.log(result.token.id);
  //     } else if (result.error) {
  //       // Error creating the token
  //       console.log(result.error.message);
  //     }
  //   });

  // this.stripe
  //   .createToken(this.cardElement.element, { name: product, currency: 'eur' })
  //   .subscribe((result) => {
  //     if (result.token) {
  //       // Use the token
  //       console.log(result.token);
  //       console.log(result.token.id);
  //     } else if (result.error) {
  //       // Error creating the token
  //       console.log(result.error.message);
  //     }
  //   });
  // }




}