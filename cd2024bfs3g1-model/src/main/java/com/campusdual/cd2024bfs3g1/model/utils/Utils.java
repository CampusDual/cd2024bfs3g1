package com.campusdual.cd2024bfs3g1.model.utils;

import com.campusdual.cd2024bfs3g1.model.core.dao.ToyDao;
import com.campusdual.cd2024bfs3g1.model.core.dao.OrderDao;
import com.campusdual.cd2024bfs3g1.model.core.dao.ShipmentDao;
import com.campusdual.cd2024bfs3g1.model.core.dao.UserDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.apache.tika.Tika;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final int JUKIDS_COMMISSION = 7;

    public static Boolean validaEmail(String email) {
        Pattern pattern = Pattern.compile("^([0-9a-zA-Z]+[-._+&])*[0-9a-zA-Z]+@([-0-9a-zA-Z]+[.])+[a-zA-Z]{2,6}$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static HashMap<String, Object> imageService( HashMap<String, Object> queryData) throws IOException {
        //String para verificar tipo correcto antes de modificar.
        String prefix = "image/";

        HashMap<String, Object> response = new HashMap<>();
        byte[] decodedBytes;
        if( queryData.containsKey("usr_name") ) {
            //Decoded Image
            decodedBytes = Base64.getDecoder().decode(
                    (String) queryData.get(UserDao.PHOTO)
            );
        } else {
            decodedBytes = Base64.getDecoder().decode(
                    (String) queryData.get(ToyDao.ATTR_PHOTO)
            );
        }


        //Agregar al response
        response.put("decodedBytes", decodedBytes);

        //Identificacion del Tipo de formato del BASE64 decoded
        Tika tika = new Tika();
        String mimeType = tika.detect(new ByteArrayInputStream(decodedBytes));


        if( mimeType.startsWith( prefix ) ) {
            mimeType = mimeType.substring(prefix.length());
        }

        MediaType contentType = new MediaType("image", mimeType);

        response.put("contentType", contentType);

        return response;
    }
    public static String getRole(){
        if(SecurityContextHolder.getContext() == null) {
            return null;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || auth.getAuthorities() == null || auth.getAuthorities().toArray().length == 0) {
            return null;
        }
        return auth.getAuthorities().toArray()[0].toString();
    }

    public static void pruneTree(Object tree, String branchToRemove){
        if(tree instanceof SQLStatementBuilder.BasicExpression){
            SQLStatementBuilder.BasicExpression be = (SQLStatementBuilder.BasicExpression) tree;
            if(be.getLeftOperand() instanceof SQLStatementBuilder.BasicExpression){
                pruneTreeBranch(be, (SQLStatementBuilder.BasicExpression) be.getLeftOperand(),branchToRemove);
            }
            if(be.getRightOperand() instanceof SQLStatementBuilder.BasicExpression){
                pruneTreeBranch(be, (SQLStatementBuilder.BasicExpression) be.getRightOperand(),branchToRemove);
            }
        }
    }
    private static void pruneTreeBranch(SQLStatementBuilder.BasicExpression tree,SQLStatementBuilder.BasicExpression branch, String branchToRemove){
        if(mustLeafBeDeleted(branch.getLeftOperand(),branchToRemove)){
            //sobrescrimimos la rama con la rama hija que permanece que es la contraria
            reWriteBranch(tree, branch,branch.getRightOperand());
        }else if(branch.getLeftOperand() instanceof SQLStatementBuilder.BasicExpression){
            pruneTreeBranch(branch, (SQLStatementBuilder.BasicExpression) branch.getLeftOperand(),branchToRemove);
        }
        if(mustLeafBeDeleted(branch.getRightOperand(),branchToRemove)){
            //sobrescrimimos la rama con la rama hija que permanece que es la contraria
            reWriteBranch(tree,branch,branch.getLeftOperand());
        }else if(branch.getRightOperand() instanceof SQLStatementBuilder.BasicExpression){
            pruneTreeBranch(branch, (SQLStatementBuilder.BasicExpression) branch.getRightOperand(),branchToRemove);
        }
    }

    private static void reWriteBranch(SQLStatementBuilder.BasicExpression parentTree,Object branchDeleted,Object remainBranch){
        if(parentTree.getRightOperand().equals(branchDeleted)){
            parentTree.setRightOperand(remainBranch);
        }else{
            parentTree.setLeftOperand(remainBranch);
        }
    }

    private static boolean mustLeafBeDeleted(Object branch, String branchToRemove){
        if(branch instanceof SQLStatementBuilder.BasicExpression){
            SQLStatementBuilder.BasicExpression be = (SQLStatementBuilder.BasicExpression) branch;
            return be.getLeftOperand() instanceof SQLStatementBuilder.BasicField && branchToRemove.equalsIgnoreCase(be.getLeftOperand().toString());
        }else{
            return false;
        }
    }

    public static String getAuthenticatedEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    public static Object idGetter(DefaultOntimizeDaoHelper daoHelper, UserDao userDao) {
        String email = getAuthenticatedEmail();

        if (email == null) {
            return createError("No estas logueado");
        }

        HashMap<String, Object> keysValues = new HashMap<>();
        keysValues.put(UserDao.LOGIN, email);
        List<String> attributes = List.of(UserDao.USR_ID);
        EntityResult userData = daoHelper.query(userDao, keysValues, attributes);

        if (userData.isEmpty() || userData.isWrong()) {
            return createError("Error al recuperar el usuario");
        }

        return userData.getRecordValues(0).get(UserDao.USR_ID);
    }

    public static EntityResult queryByStatusBuyer(DefaultOntimizeDaoHelper daoHelper, Object dao, UserDao userDao, Map<String, Object> keyMap, List<String> attrList, int status, String queryName) {
        Integer idUser = (Integer) idGetter(daoHelper, userDao);
        keyMap.put(OrderDao.ATTR_BUYER_ID, idUser);
        keyMap.put(ToyDao.ATTR_TRANSACTION_STATUS, status);
        if (dao instanceof ShipmentDao) {
            return daoHelper.query((ShipmentDao) dao, keyMap, attrList, queryName);
        } else if (dao instanceof OrderDao) {
            return daoHelper.query((OrderDao) dao, keyMap, attrList, queryName);
        }
        return Utils.createError("Error en queryByStatusBuyer");
    }

    public static EntityResult queryByStatusSeller(DefaultOntimizeDaoHelper daoHelper, Object dao, UserDao userDao, Map<String, Object> keyMap, List<String> attrList, int status, String queryName) {
        Integer idUser = (Integer) idGetter(daoHelper, userDao);
        keyMap.put(UserDao.USR_ID, idUser);
        keyMap.put(ToyDao.ATTR_TRANSACTION_STATUS, status);
        return daoHelper.query((ToyDao) dao, keyMap, attrList, queryName);
    }

    public static EntityResult fetchToyData(DefaultOntimizeDaoHelper daoHelper, ToyDao toyDao, Integer toyId) {
        HashMap<String, Object> toyKeyValues = new HashMap<>();
        toyKeyValues.put(ToyDao.ATTR_ID, toyId);
        List<String> toyAttributes = Arrays.asList(ToyDao.ATTR_PRICE, ToyDao.ATTR_TRANSACTION_STATUS);
        return daoHelper.query(toyDao, toyKeyValues, toyAttributes);
    }

    public static double calculateTotalPrice(EntityResult toyData) {
        BigDecimal toyPriceDecimal = (BigDecimal) toyData.getRecordValues(0).get(ToyDao.ATTR_PRICE);
        double toyPrice = toyPriceDecimal.doubleValue();
        return toyPrice / (1 - (double) JUKIDS_COMMISSION / 100);
    }

    public static double calculateTotalPriceWithShipment(EntityResult toyData, double shipmentPrice) {
        double totalPrice = calculateTotalPrice(toyData);
        return totalPrice + shipmentPrice;
    }

    public static double getShipmentPrice(Map<String, Object> shipmentData) {
        Integer priceInteger = (Integer) shipmentData.get(ShipmentDao.ATTR_PRICE);
        return priceInteger.doubleValue();
    }

    public static boolean isToyAvailable(EntityResult toyData) {
        Integer available = (Integer) toyData.getRecordValues(0).get(ToyDao.ATTR_TRANSACTION_STATUS);
        return available == 0;
    }

    public static EntityResult insertOrder(DefaultOntimizeDaoHelper daoHelper, OrderDao orderDao, Map<String, Object> orderData) {
        return daoHelper.insert(orderDao, orderData);
    }

    public static EntityResult updateToyStatus(DefaultOntimizeDaoHelper daoHelper, ToyDao toyDao, Integer toyId, Integer status) {
        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put(ToyDao.ATTR_TRANSACTION_STATUS, status);
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put(ToyDao.ATTR_ID, toyId);
        return daoHelper.update(toyDao, updateStatus, keyMap);
    }

    public static EntityResult createMessageResult(String message) {
        EntityResult result = new EntityResultMapImpl();
        result.setMessage(message);
        return result;
    }

    public static EntityResult createError(String mensaje) {
        EntityResult errorEntityResult = new EntityResultMapImpl();
        errorEntityResult.setCode(EntityResult.OPERATION_WRONG);
        errorEntityResult.setMessage(mensaje);
        return errorEntityResult;
    }
}
