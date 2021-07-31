package de.gitlab.kaisoenmalex.api.network.packets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class is sent between server and client to perform an action.
 *
 * @author Alexander Ley
 * @version 1.2
 */
public class ActionPacket implements Serializable {

    private final String methodName;
    private List<Object> parameters;

    /**
     * Parameters are empty -> for void methods.
     * @param methodName Name of server method in server ActionHandler.
     */
    public ActionPacket(String methodName){
        this.methodName = methodName;
        this.parameters = Collections.emptyList();
    }

    /**
     * @param methodName Name of server method in server ActionHandler.
     * @param parameters Parameters of method on server ActionHandler.
     */
    public ActionPacket(String methodName, Object... parameters) {
        this(methodName, Arrays.asList(parameters));
    }

    /**
     * @param methodName Name of server method in server ActionHandler.
     * @param parameters Parameters of method on server ActionHandler.
     */
    public ActionPacket(String methodName, List<Object> parameters) {
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void addParameter(Object obj){
        if (parameters.isEmpty()){
            parameters = new ArrayList<>(Collections.singletonList(obj));
        }
        else {
            try{
                parameters.add(obj);
            }
            catch (Exception ex){
                final ArrayList<Object> list = new ArrayList<>(parameters);
                list.add(obj);
                parameters = list;
            }
        }
    }
}