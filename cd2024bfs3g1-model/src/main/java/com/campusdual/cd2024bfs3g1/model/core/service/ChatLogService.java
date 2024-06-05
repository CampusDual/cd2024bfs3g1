package com.campusdual.cd2024bfs3g1.model.core.service;

import com.campusdual.cd2024bfs3g1.api.core.service.IChatLogService;
import com.campusdual.cd2024bfs3g1.model.core.dao.ChatLogDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("ChatLogService")
public class ChatLogService implements IChatLogService {

    @Autowired
    private ChatLogDao chatLogDao;

    @Autowired
    private DefaultOntimizeDaoHelper daoHelper;

    @Override
    public EntityResult chatLogQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException {
        return this.daoHelper.query(this.chatLogDao, keyMap, attrList);
    }

    @Override
    public EntityResult chatLogInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
        return this.daoHelper.insert(this.chatLogDao, attrMap);
    }
}
