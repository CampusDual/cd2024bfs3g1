package com.campusdual.cd2024bfs3g1.ws.core.rest;

import com.campusdual.cd2024bfs3g1.api.core.service.IOrderService;
import com.campusdual.cd2024bfs3g1.api.core.service.IShipmentService;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.server.rest.ORestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

@RestController
@RequestMapping("/shipments")
public class ShipmentRestController extends ORestController<IShipmentService> {

    @Autowired
    private IShipmentService shipmentService;

    @Override
    public IShipmentService getService() { return this.shipmentService; }

}
