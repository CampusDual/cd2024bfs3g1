package com.campusdual.cd2024bfs3g1.model.core.service;

import com.campusdual.cd2024bfs3g1.api.core.service.IToyService;
import com.campusdual.cd2024bfs3g1.model.core.dao.ToyDao;
import com.campusdual.cd2024bfs3g1.model.utils.Utils;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("ToyService")
@Lazy
public class ToyService implements IToyService {

    @Autowired
    private ToyDao toyDao;
    @Autowired
    private DefaultOntimizeDaoHelper daoHelper;

    @Override
    public EntityResult toyQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException {
         return this.daoHelper.query(this.toyDao, keyMap, attrList);
    }

    //Prueba mia
    @Override
    public EntityResult byUserIdQuery(int usr_id) throws OntimizeJEERuntimeException{
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("usr_id",usr_id);
        List<String> attrList = Arrays.asList("toyid","name","price");
        return this.toyQuery(keyMap, attrList);
    }

    @Override
    public EntityResult toyInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {

        if(!Utils.validaEmail((String) attrMap.get("email"))) {
            //System.out.println("Email invalido");
            EntityResult error = new EntityResultMapImpl();
            error.setCode(EntityResult.OPERATION_WRONG);
            error.setMessage("El correo electrónico no es correcto");
            return error;
        }

        return this.daoHelper.insert(this.toyDao,attrMap);
    }

    @Override
    public EntityResult toyUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
        return this.daoHelper.update(this.toyDao, attrMap, keyMap);
    }

    @Override
    public EntityResult toyDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
        return this.daoHelper.delete(this.toyDao, keyMap);
    }
}


