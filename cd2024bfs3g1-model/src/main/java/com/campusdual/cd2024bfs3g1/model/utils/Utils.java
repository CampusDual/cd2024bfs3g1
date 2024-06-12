package com.campusdual.cd2024bfs3g1.model.utils;

import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.campusdual.cd2024bfs3g1.model.core.dao.UserDao;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.campusdual.cd2024bfs3g1.model.core.dao.ToyDao;
import org.apache.tika.Tika;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static Boolean validaEmail (String email) {
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
}
