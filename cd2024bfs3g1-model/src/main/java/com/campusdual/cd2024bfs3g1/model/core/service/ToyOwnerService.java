package com.campusdual.cd2024bfs3g1.model.core.service;

import com.campusdual.cd2024bfs3g1.api.core.service.IToyOwnerService;
import com.campusdual.cd2024bfs3g1.model.core.dao.OrderDao;
import com.campusdual.cd2024bfs3g1.model.core.dao.ShipmentDao;
import com.campusdual.cd2024bfs3g1.model.core.dao.ToyDao;
import com.campusdual.cd2024bfs3g1.model.core.dao.UserDao;
import com.campusdual.cd2024bfs3g1.model.utils.Utils;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.gui.SearchValue;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("ToyOwnerService")
@Lazy
public class ToyOwnerService implements IToyOwnerService {

    public static final String ADMIN = "admin";
    @Autowired
    private ToyDao toyDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private DefaultOntimizeDaoHelper daoHelper;

    //Muestra juguetes del estado 0 al vendedor
    @Override
    public EntityResult toyQuery(Map<String, Object> keyMap, List<String> attrList) {
        return Utils.queryByStatusSeller(daoHelper, toyDao, userDao, keyMap, attrList, ToyDao.STATUS_AVAILABLE, null);
    }

    //Muestra juguetes del estado 1 al vendedor
    public EntityResult pendingSendQuery(Map<String, Object> keyMap, List<String> attrList) {
        keyMap.put(OrderDao.ATTR_SESSION_ID, new SearchValue(SearchValue.NOT_NULL, null));
        return Utils.queryByStatusSeller(daoHelper, toyDao, userDao, keyMap, attrList, ToyDao.STATUS_PENDING_SHIPMENT, ToyDao.QUERY_TOY_JOIN);
    }

    //Muestra juguetes del estado 2 al vendedor
    @Override
    public EntityResult pendingConfirmQuery(Map<String, Object> keyMap, List<String> attrList) {

        Integer idUser = (Integer) Utils.idGetter(daoHelper, userDao);
        keyMap.put(ToyDao.ATTR_USR_ID, idUser);
        keyMap.put(ToyDao.ATTR_TRANSACTION_STATUS, new SearchValue(SearchValue.IN, Arrays.asList(ToyDao.STATUS_SENT, ToyDao.STATUS_RECEIVED)));

        return this.daoHelper.query(this.toyDao, keyMap, attrList, ToyDao.QUERY_TOY_JOIN);
    }

    //Muestra juguetes del estado 4 y 5 al vendedor
    @Override
    public EntityResult toySoldQuery(Map<String, Object> keyMap, List<String> attrList) {
        Integer idUser = (Integer) Utils.idGetter(daoHelper, userDao);
        keyMap.put(ToyDao.ATTR_USR_ID, idUser);
        keyMap.put(OrderDao.ATTR_SESSION_ID, new SearchValue(SearchValue.NOT_NULL, null));
        keyMap.put(ToyDao.ATTR_TRANSACTION_STATUS, new SearchValue(SearchValue.IN, Arrays.asList(ToyDao.STATUS_PURCHASED, ToyDao.STATUS_RATED)));

        return this.daoHelper.query(this.toyDao, keyMap, attrList, ToyDao.QUERY_TOY_ORDER);
    }

    //Muestra juguetes reservados (pendientes de pago) al vendedor
    @Override
    public EntityResult reservedQuery(Map<String, Object> keyMap, List<String> attrList) {

        Integer idUser = (Integer) Utils.idGetter(daoHelper, userDao);
        keyMap.put(ToyDao.ATTR_USR_ID, idUser);
        keyMap.put(OrderDao.ATTR_SESSION_ID, new SearchValue(SearchValue.NULL, null));
        keyMap.put(ToyDao.ATTR_TRANSACTION_STATUS,
                new SearchValue(SearchValue.IN, Arrays.asList(ToyDao.STATUS_PENDING_SHIPMENT, ToyDao.STATUS_PURCHASED)));
        return this.daoHelper.query(this.toyDao, keyMap, attrList, ToyDao.QUERY_TOY_ORDER_USER);
    }

    @Override
    public EntityResult toyInsert(Map<String, Object> attrMap) {

        Integer idUser = (Integer) Utils.idGetter(daoHelper, userDao);
        attrMap.put(UserDao.USR_ID, idUser);
        return this.daoHelper.insert(this.toyDao, attrMap);
    }

    @Override
    public EntityResult toyUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
        if (!isOwnerOrAdmin((Integer) keyMap.get(ToyDao.ATTR_ID))) {
            return Utils.createError("No tienes permisos para actualizar este juguete: ");
        }
        return this.daoHelper.update(this.toyDao, attrMap, keyMap);
    }

    @Override
    public EntityResult toyDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
        if (!isOwnerOrAdmin((Integer) keyMap.get(ToyDao.ATTR_ID))) {
            return Utils.createError("No tienes permisos para borrar este juguete: ");
        }
        return this.daoHelper.delete(this.toyDao, keyMap);
    }

    private boolean isOwnerOrAdmin(Integer toyId){
        if(ADMIN.equalsIgnoreCase(Utils.getRole())){
            return true;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userLogin = auth.getName();
        if (userLogin == null) {
            return false;
        }
        //Obtenemos el id del usuario que hace la petición
        HashMap<String, Object> keyUserValues = new HashMap<>();
        keyUserValues.put(UserDao.LOGIN, userLogin);
        List<String> attrList = Arrays.asList(UserDao.USR_ID);
        EntityResult userData = this.daoHelper.query(userDao, keyUserValues, attrList);
        if (userData.isWrong()||userData.isEmpty()) {
            return false;
        }
        Integer idUser = (Integer) userData.getRecordValues(0).get(UserDao.USR_ID);

        //Obtenemos el id del dueño del juguete
        HashMap<String, Object> keyToyValues = new HashMap<>();
        keyToyValues.put(ToyDao.ATTR_ID, toyId);
        List<String> toyList = Arrays.asList(ToyDao.ATTR_USR_ID);
        EntityResult toyData = this.daoHelper.query(toyDao, keyToyValues, toyList);
        if (toyData.isWrong()||toyData.isEmpty()) {
            return false;
        }
        Integer toyIdUser = (Integer) toyData.getRecordValues(0).get(ToyDao.ATTR_USR_ID);
        return idUser.equals(toyIdUser);
    }
}