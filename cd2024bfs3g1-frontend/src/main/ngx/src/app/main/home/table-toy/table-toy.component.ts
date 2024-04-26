import { Component,OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogService, OGridComponent, OntimizeService } from 'ontimize-web-ngx';
import { ToysMapService } from 'src/app/shared/services/toys-map.service';
import { calculateDistanceFunction } from 'src/app/shared/shared.module';

@Component({
  selector: 'app-table-toy',
  templateUrl: './table-toy.component.html',
  styleUrls: ['./table-toy.component.css']
})

export class TableToyComponent {
  @ViewChild('toysGrid') protected toyGrid: OGridComponent;
  private location: any;
  
  constructor(    
    private router: Router,
    private actRoute: ActivatedRoute,
    private ontimizeService: OntimizeService,
    protected dialogService: DialogService,
    private toysMapService: ToysMapService    
  ) 
  {
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

  navigate() {
    this.router.navigate(['../', 'login'], { relativeTo: this.actRoute });
  }

  //Obtencion de latitud y longitud del mapa y llamada al servicio para pasarle los datos
  getPosition(e) {
    if (this.dialogService) {
        if(window.confirm('¿Desea buscar para esta ubicación?'))
        {
          this.toysMapService.setLocation(e.latlng.lat, e.latlng.lng);

          console.log(this.location.latitude);
          }     
    }
    
  }

  //Se calcula la distancia a la que se encuentra el objeto al punto del mapa que sea a seleccionado previamente
  calculateDistance(rowData: any): number {    
    const R: number = 6371; // Radio de la Tierra en kilómetros      
    let lat1:number = this.location.latitude;    
    let lon1: number = this.location.longitude;

    let lat2: number = rowData['latitude'];
    let lon2: number = rowData['longitude'];

    function deg2rad(deg: number): number {
      return deg * (Math.PI / 180);
    }

    let dLat: number = deg2rad(lat2 - lat1);
    let dLon: number = deg2rad(lon2 - lon1);
    let a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
      + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
      * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    let c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    let distance = R * c;
    return Math.round(distance * 100.0) / 100.0; // Redondear a 2 decimales
  }

  //Se añade una localización a los datos recogidos del grid y existe un punto en el mapa
  addLocation(e){    
    if(this.location.latitude != undefined || this.location.longitude != undefined){
      e.forEach(element =>{
        element.location = this.calculateDistance(element);
      })
      console.log(e);
      this.toyGrid;
    }   
  }
}
