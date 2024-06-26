package com.campusdual.cd2024bfs3g1.model.core.service;

import com.campusdual.cd2024bfs3g1.api.core.service.IOrderService;
import com.campusdual.cd2024bfs3g1.model.core.dao.OrderDao;
import com.campusdual.cd2024bfs3g1.model.core.dao.ShipmentDao;
import com.campusdual.cd2024bfs3g1.model.core.dao.ToyDao;
import com.campusdual.cd2024bfs3g1.model.core.dao.UserDao;
import com.campusdual.cd2024bfs3g1.model.utils.Utils;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.gui.SearchValue;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;


@Service("OrderService")
@Lazy
public class OrderService implements IOrderService {

    @Autowired
    private OrderDao orderDao;
    @Autowired
    private ToyDao toyDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private DefaultOntimizeDaoHelper daoHelper;

    @Override
    public EntityResult orderQuery(Map<String, Object> keyMap, List<String> attrList) {

        Integer idUser = (Integer) Utils.idGetter(daoHelper, userDao);
        keyMap.put(OrderDao.ATTR_BUYER_ID, idUser);
        return this.daoHelper.query(this.orderDao, keyMap, attrList);
    }

    //Muestra juguetes estado 4 y 5 al comprador
    @Override
    public EntityResult purchasedQuery(Map<String, Object> keyMap, List<String> attrList) {

        Integer idUser = (Integer) Utils.idGetter(daoHelper, userDao);
        keyMap.put(OrderDao.ATTR_BUYER_ID, idUser);
        keyMap.put(ToyDao.ATTR_TRANSACTION_STATUS,
                new SearchValue(SearchValue.IN, Arrays.asList(ToyDao.STATUS_PURCHASED, ToyDao.STATUS_RATED)));
        keyMap.put(OrderDao.ATTR_SESSION_ID, new SearchValue(SearchValue.NOT_NULL, null));
        return this.daoHelper.query(this.orderDao, keyMap, attrList, OrderDao.QUERY_JOIN_ORDERS_TOYS);
    }

    //Muestra juguetes reservados (pendientes de pago) al comprador
    @Override
    public EntityResult reservedQuery(Map<String, Object> keyMap, List<String> attrList) {

        Integer idUser = (Integer) Utils.idGetter(daoHelper, userDao);
        keyMap.put(OrderDao.ATTR_BUYER_ID, idUser);
        keyMap.put(OrderDao.ATTR_SESSION_ID, new SearchValue(SearchValue.NULL, null));
        keyMap.put(ToyDao.ATTR_TRANSACTION_STATUS,
                new SearchValue(SearchValue.IN, Arrays.asList(ToyDao.STATUS_PENDING_SHIPMENT, ToyDao.STATUS_PURCHASED)));
        return this.daoHelper.query(this.orderDao, keyMap, attrList, OrderDao.QUERY_JOIN_ORDERS_TOYS);
    }

    @Override
    @Transactional
    public EntityResult orderInsert(Map<String, Object> orderData) {

        String email = Utils.getAuthenticatedEmail();
        Integer idUser = (Integer) Utils.idGetter(daoHelper, userDao);

        //Poblamos orderData, el HashMap que usaremos para el insert en ORDERS

        populateOrderData(orderData, idUser, email);

        //Recuperamos TOY - PRICE y TOY - TRANSACTION_STATUS

        Integer toyId = (Integer) orderData.get(OrderDao.ATTR_TOY_ID);
        EntityResult toyData = Utils.fetchToyData(daoHelper, toyDao, toyId);
        if (toyData.isWrong() || toyData.isEmpty()) {
            return Utils.createError("Error al recuperar el precio del juguete!");
        }

        //Calculamos ORDER - TOTAL_PRICE

        double totalPrice = Utils.calculateTotalPrice(toyData);
        orderData.put(OrderDao.ATTR_TOTAL_PRICE, totalPrice);

        //Verificamos disponibilidad del juguete e insertamos en ORDERS

        if (!Utils.isToyAvailable(toyData) && !isReserved(toyId, idUser)) {

            return Utils.createError("El producto no se encuentra disponible");
        }

        //Comprobamos si existen reservas previas de este usuario y las borramos

        deletePreviousReserves(idUser);

        EntityResult orderResult = Utils.insertOrder(daoHelper, orderDao, orderData);

        if (orderResult.isWrong()) {
            return Utils.createError("Error al crear la orden");
        }

        //Actualizamos TOYS - TRANSACTION_STATUS (0 -> 4)

        EntityResult toyUpdateResult = Utils.updateToyStatus(daoHelper, toyDao, toyId, ToyDao.STATUS_PURCHASED);

        if (toyUpdateResult.isWrong()) {
            return Utils.createError("Error al actualizar el transaction_status");
        }

        return Utils.createMessageResult("Orden creada correctamente");
    }

    public static void populateOrderData(Map<String, Object> orderData, Integer idUser, String email) {
        orderData.put(OrderDao.ATTR_BUYER_ID, idUser);
        orderData.put(OrderDao.ATTR_BUYER_EMAIL, email);
        orderData.put(OrderDao.ATTR_ORDER_DATE, LocalDateTime.now());
    }

    @Override
    @Transactional
    public EntityResult orderAndShipmentInsert(Map<String, Object> shipmentData) {

        //Extraemos el TOY - TOYID

        Integer toyId = (Integer) shipmentData.remove(ToyDao.ATTR_ID);

        //Recuperamos ORDER - BUYER_ID, BUYER_EMAIL
        //Generamos ORDER - ORDER_DATE

        String email = Utils.getAuthenticatedEmail();
        Integer idUser = (Integer) Utils.idGetter(daoHelper, userDao);

        //Creamos y poblamos orderData, el HashMap que usaremos para el insert en ORDERS

        Map<String, Object> orderData = new HashMap<>();
        populateOrderShipData(orderData, idUser, email, toyId);

        //Recuperamos TOY - PRICE y TOY - TRANSACTION_STATUS

        EntityResult toyData = Utils.fetchToyData(daoHelper, toyDao, toyId);

        if (toyData.isWrong() || toyData.isEmpty()) {
            return Utils.createError("Error al recuperar el precio del juguete!");
        }

        //Recuperamos SHIPMENTS - PRICE
        //Calculamos ORDER - TOTAL_PRICE

        double shipmentPrice = Utils.getShipmentPrice(shipmentData);
        double totalPrice = Utils.calculateTotalPriceWithShipment(toyData, shipmentPrice);
        orderData.put(OrderDao.ATTR_TOTAL_PRICE, totalPrice);

        //Verificamos disponibilidad del juguete e insertamos en ORDERS

        if (!Utils.isToyAvailable(toyData) && !isReserved(toyId, idUser)) {

            return Utils.createError("El producto no se encuentra disponible");
        }

        //Comprobamos si existen reservas previas de este usuario y las borramos

        deletePreviousReserves(idUser);

        EntityResult orderResult = Utils.insertOrder(daoHelper, orderDao, orderData);

        if (orderResult.isWrong()) {
            return Utils.createError("Error al crear la orden");
        }

        //Recuperamos ORDERS - ORDER_ID
        //Asignamos SHIPMENTS - ORDER_ID

        shipmentData.put(ShipmentDao.ATTR_ORDER_ID, orderResult.get(OrderDao.ATTR_ID));

        //Insertamos en SHIPMENTS

        EntityResult shipmentResult = insertShipment(shipmentData);

        if (shipmentResult.isWrong()) {
            return Utils.createError("Error al crear el envío");
        }

        //Actualizamos TOYS - TRANSACTION_STATUS (0 -> 1)

        EntityResult toyUpdateResult = Utils.updateToyStatus(daoHelper, toyDao, toyId, ToyDao.STATUS_PENDING_SHIPMENT);

        if (toyUpdateResult.isWrong()) {
            return Utils.createError("Error al actualizar el transaction_status");
        }

        return Utils.createMessageResult("Orden y envío creados correctamente");
    }

    public static void populateOrderShipData(Map<String, Object> orderData, Integer idUser, String email, Integer toyId) {
        orderData.put(OrderDao.ATTR_BUYER_ID, idUser);
        orderData.put(OrderDao.ATTR_BUYER_EMAIL, email);
        orderData.put(OrderDao.ATTR_ORDER_DATE, LocalDateTime.now());
        orderData.put(OrderDao.ATTR_TOY_ID, toyId);
    }

    public EntityResult insertShipment(Map<String, Object> shipmentData) {
        shipmentData.put(ShipmentDao.ATTR_TRACKING_NUMBER, "0000000000");
        return this.daoHelper.insert(this.shipmentDao, shipmentData);
    }

    public boolean isReserved(int toyId, int buyerId) {

        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(OrderDao.ATTR_TOY_ID, toyId);
        keyMap.put(OrderDao.ATTR_BUYER_ID, buyerId);
        keyMap.put(OrderDao.ATTR_SESSION_ID, new SearchValue(SearchValue.NULL, null));
        List<String> attrList = List.of(OrderDao.ATTR_ID);
        EntityResult queryResult = daoHelper.query(orderDao, keyMap, attrList);

        return !queryResult.isEmpty();
    }

    private void deletePreviousReserves(Integer idUser) {

        //Recuperamos Orders de este usuario sin finalizar

        Map<String, Object> searchValues = new HashMap<>();
        searchValues.put(OrderDao.ATTR_BUYER_ID, idUser);
        searchValues.put(OrderDao.ATTR_SESSION_ID, new SearchValue(SearchValue.NULL, null));

        List<String> resultAttributes = List.of(OrderDao.ATTR_ID, OrderDao.ATTR_TOY_ID);
        EntityResult idData = this.daoHelper.query(this.orderDao, searchValues, resultAttributes);

        Integer previousOrderId = (Integer) idData.getRecordValues(0).get(OrderDao.ATTR_ID);
        Integer previousToyId = (Integer) idData.getRecordValues(0).get(OrderDao.ATTR_TOY_ID);

        //Comprobamos si hay SHIPMENT asociado y recuperamos su ID en caso afirmativo

        Map<String, Object> shipmentSearchValues = new HashMap<>();
        shipmentSearchValues.put(ShipmentDao.ATTR_ORDER_ID, previousOrderId);
        List<String> shipmentResultAttributes = List.of(ShipmentDao.ATTR_ID);
        EntityResult shipmentIdData = this.daoHelper.query(this.shipmentDao, shipmentSearchValues, shipmentResultAttributes);

        if (!shipmentIdData.isEmpty()) {

            //Borramos el SHIPMENT si existe

            Integer shipmentId = (Integer) shipmentIdData.getRecordValues(0).get(ShipmentDao.ATTR_ID);

            Map<String, Object> deleteShipKeyMap = new HashMap<>();
            deleteShipKeyMap.put(ShipmentDao.ATTR_ID, shipmentId);
            this.daoHelper.delete(this.shipmentDao, deleteShipKeyMap);
        }

        //Borramos el ORDER antiguo

        Map<String, Object> deleteKeyMap = new HashMap<>();
        deleteKeyMap.put(OrderDao.ATTR_ID, previousOrderId);
        this.daoHelper.delete(this.orderDao, deleteKeyMap);

        //Ponemos el juguete en disponible

        Utils.updateToyStatus(daoHelper, toyDao, previousToyId, ToyDao.STATUS_AVAILABLE);
    }
}