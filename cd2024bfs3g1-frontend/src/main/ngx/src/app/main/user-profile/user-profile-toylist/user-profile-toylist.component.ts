import { ChangeDetectionStrategy, Component, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { MatTabGroup } from '@angular/material/tabs';
import { JukidsAuthService } from 'src/app/shared/services/jukids-auth.service';
import { AuthService, OTableBase, OTableColumnComponent, OTextInputComponent, OntimizeService } from 'ontimize-web-ngx';
import { OTranslateService, DialogService, ODialogConfig, SnackBarService, OSnackBarConfig } from 'ontimize-web-ngx';
import { AnyCatcher } from 'rxjs/internal/AnyCatcher';
import { UserInfoService } from 'src/app/shared/services/user-info.service';
import { from } from 'rxjs';

@Component({
  selector: 'app-user-profile-toylist',
  templateUrl: './user-profile-toylist.component.html',
  styleUrls: ['./user-profile-toylist.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush

})

export class UserProfileToylistComponent {
  private STATUS_AVAILABLE: Number = 0;
  private STATUS_PENDING_SHIPMENT: Number = 1;
  private STATUS_SENT: Number = 2;
  private STATUS_RECEIVED: Number = 3;
  private STATUS_PURCHASED: Number = 4;

  //Indice inicial para pestañas de tablas
  public currentToysTabIndex = 0; //First Tab

  @ViewChild('tableSend') protected tableSend :OTableBase ;
  @ViewChild('senderAddress') protected senderAddress :OTextInputComponent;
  @ViewChild('senderAddress') protected shipmentAddress: OTableColumnComponent;
  @ViewChild('senderAddress') protected shipmentCompany: OTableColumnComponent;
  @ViewChild('toysTabs') toysTabGroup: MatTabGroup;

  public userInfo;
  private redirect = '/toys';

  constructor(
    private jukidsAuthService: JukidsAuthService,
    private router: Router,
    private translate: OTranslateService,
    protected dialogService: DialogService,
    protected snackBarService: SnackBarService,
    private oServiceShipment: OntimizeService,
    public userInfoService: UserInfoService) {

    const conf2 = this.oServiceShipment.getDefaultServiceConfiguration('shipments');
    this.oServiceShipment.configureService(conf2);

    this.userInfo = this.userInfoService.getUserInfo();
    if (!this.jukidsAuthService.isLoggedIn()) {
      const self = this;
      self.router.navigate([this.redirect]);
    }
  }

  //Metodo de confirmar el envio por parte del vendedor 
  //(con doble confirmacion, mensaje de confirmación y salto a la siguiente pestaña actualizada)
  public sendSubmit(e: any) {
    if (this.dialogService) {
      this.dialogService.confirm(this.translate.get('CONFIRMATION_TITLE'), this.translate.get('OK_CONFIRMATION'));
      this.dialogService.dialogRef.afterClosed().subscribe( result => {
        if(result) {
          this.currentToysTabIndex = this.toysTabGroup.selectedIndex; //recoge el indice de pestaña actual          
          const kv = { "toyid": e };
          const av = { "sender_address": this.senderAddress.getValue(),"transaction_status": this.STATUS_SENT }
          this.oServiceShipment.update(kv, av, "shipmentSent").subscribe(result => {
          this.tableSend.refresh();
          this.currentToysTabIndex = this.currentToysTabIndex + 1; //actualica el indice de pestaña a la siguiente una vez confirmado
          })
          this.snackBarService.open(this.translate.get('CONFIRMED'));
        } else {
          this.snackBarService.open(this.translate.get('CANCELLED'));
        }
      })
    }
  }
  
}
