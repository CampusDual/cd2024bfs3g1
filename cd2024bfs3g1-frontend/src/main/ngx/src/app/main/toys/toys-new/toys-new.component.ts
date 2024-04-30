import { Component, ViewChild} from '@angular/core';
import { ORealInputComponent, OntimizeService } from 'ontimize-web-ngx';
import { Subscription } from 'rxjs';
import { ToysMapService } from 'src/app/shared/services/toys-map.service';

@Component({
  selector: 'app-toys-new',
  templateUrl: './toys-new.component.html',
  styleUrls: ['./toys-new.component.css']
})
export class ToysNewComponent{
  
  @ViewChild('NewToy') protected formToy: any;
  @ViewChild('latitude') protected lat: ORealInputComponent;
  @ViewChild('longitude') protected lon: ORealInputComponent;

  private location: any;
  subscription:Subscription;

  constructor(
    private ontimizeService: OntimizeService,
    private toysMapService: ToysMapService
  ){
  //Configuración del servicio para poder ser usado
  const conf = this.ontimizeService.getDefaultServiceConfiguration('toys');
  this.ontimizeService.configureService(conf);
  }

  ngOnInit() {
    //Se escuchan los cambios del servicio
    this.toysMapService.getLocation().subscribe(data => {
      this.location = data;
    });
  }

  handleMapClick(e) {
    let latitude = this.location.latitude;
    let longitude = this.location.longitude;

    this.lat.setValue(latitude);
    this.lon.setValue(longitude);
  }
}
