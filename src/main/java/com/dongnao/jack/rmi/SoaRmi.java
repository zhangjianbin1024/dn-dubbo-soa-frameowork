package com.dongnao.jack.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SoaRmi extends Remote {
    String invoke(String param) throws RemoteException;
}
