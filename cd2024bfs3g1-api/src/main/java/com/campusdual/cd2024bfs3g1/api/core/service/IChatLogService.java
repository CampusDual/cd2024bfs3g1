package com.campusdual.cd2024bfs3g1.api.core.service;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

import java.util.List;
import java.util.Map;

public interface IChatLogService {

    EntityResult chatLogQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;

    EntityResult chatLogInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;

    EntityResult getChatLastConversationQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;

    EntityResult getLoggedChatListQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;
}
